package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.directionToCommand;

import java.util.Map;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.common.collect.ImmutableMap;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderParamsSupplier;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.UserSettings;

import rx.Observable;

public final class PositionSwitcher {

    private final OrderUtil orderUtil;
    private final Position position;
    private final Instrument instrument;
    private final OrderParamsSupplier orderParamsSupplier;
    private final RestoreSLTPPolicy noRestoreSLTPPolicy = new NoRestorePolicy();
    private Map<OrderDirection, FSMState> nextStatesByDirection;
    private final StateMachineConfig<FSMState, FSMTrigger> fsmConfig = new StateMachineConfig<>();
    private final StateMachine<FSMState, FSMTrigger> fsm = new StateMachine<>(FSMState.FLAT, fsmConfig);

    private enum FSMState {
        FLAT,
        LONG,
        SHORT,
        BUSY
    }

    private enum FSMTrigger {
        FLAT,
        BUY,
        SELL,
        MERGE_DONE,
        CLOSE_DONE
    }

    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);
    private final static String defaultMergePrefix = userSettings.defaultMergePrefix();

    public PositionSwitcher(final Instrument instrument,
                            final OrderUtil orderUtil,
                            final OrderParamsSupplier orderParamsSupplier) {
        this.instrument = instrument;
        this.orderUtil = orderUtil;
        this.orderParamsSupplier = orderParamsSupplier;
        position = orderUtil.position(instrument);

        configureFSM();
    }

    private final void configureFSM() {
        nextStatesByDirection = ImmutableMap.<OrderDirection, FSMState> builder()
                .put(OrderDirection.FLAT, FSMState.FLAT)
                .put(OrderDirection.LONG, FSMState.LONG)
                .put(OrderDirection.SHORT, FSMState.SHORT)
                .build();

        fsmConfig.configure(FSMState.FLAT)
                .permit(FSMTrigger.BUY, FSMState.BUSY)
                .permit(FSMTrigger.SELL, FSMState.BUSY)
                .ignore(FSMTrigger.FLAT)
                .ignore(FSMTrigger.CLOSE_DONE)
                .ignore(FSMTrigger.MERGE_DONE);

        fsmConfig.configure(FSMState.LONG)
                .permit(FSMTrigger.FLAT, FSMState.BUSY)
                .permit(FSMTrigger.SELL, FSMState.BUSY)
                .ignore(FSMTrigger.BUY)
                .ignore(FSMTrigger.CLOSE_DONE)
                .ignore(FSMTrigger.MERGE_DONE);

        fsmConfig.configure(FSMState.SHORT)
                .permit(FSMTrigger.FLAT, FSMState.BUSY)
                .permit(FSMTrigger.BUY, FSMState.BUSY)
                .ignore(FSMTrigger.SELL)
                .ignore(FSMTrigger.CLOSE_DONE)
                .ignore(FSMTrigger.MERGE_DONE);

        fsmConfig.configure(FSMState.BUSY)
                .onEntryFrom(FSMTrigger.BUY, () -> executeOrderCommandSignal(OrderDirection.LONG))
                .onEntryFrom(FSMTrigger.SELL, () -> executeOrderCommandSignal(OrderDirection.SHORT))
                .onEntryFrom(FSMTrigger.FLAT,
                             () -> orderUtil.closePosition(instrument).subscribe(() -> fsm.fire(FSMTrigger.CLOSE_DONE)))
                .permitDynamic(FSMTrigger.MERGE_DONE, () -> nextStatesByDirection.get(position.direction()))
                .permit(FSMTrigger.CLOSE_DONE, FSMState.FLAT)
                .ignore(FSMTrigger.FLAT)
                .ignore(FSMTrigger.BUY)
                .ignore(FSMTrigger.SELL);
    }

    public final void sendBuySignal() {
        fsm.fire(FSMTrigger.BUY);
    }

    public final void sendSellSignal() {
        fsm.fire(FSMTrigger.SELL);
    }

    public final void sendFlatSignal() {
        fsm.fire(FSMTrigger.FLAT);
    }

    private final void executeOrderCommandSignal(final OrderDirection desiredDirection) {
        final OrderCommand newOrderCommand = directionToCommand(desiredDirection);
        final OrderParams adaptedOrderParams = adaptedOrderParams(newOrderCommand);
        final String mergeLabel = defaultMergePrefix + adaptedOrderParams.label();

        final Observable<OrderEvent> mergeObs =
                Observable.defer(() -> orderUtil.mergePositionOrders(mergeLabel,
                                                                     instrument,
                                                                     noRestoreSLTPPolicy));

        orderUtil.submitOrder(adaptedOrderParams)
                .concatWith(mergeObs)
                .toCompletable()
                .subscribe(error -> fsm.fire(FSMTrigger.MERGE_DONE),
                           () -> fsm.fire(FSMTrigger.MERGE_DONE));
    }

    private final OrderParams adaptedOrderParams(final OrderCommand newOrderCommand) {
        final double absPositionExposure = Math.abs(position.signedExposure());
        final OrderParams orderParams = orderParamsSupplier.forCommand(newOrderCommand);
        return orderParams.clone()
                .withOrderCommand(newOrderCommand)
                .withAmount(orderParams.amount() + absPositionExposure)
                .build();
    }
}
