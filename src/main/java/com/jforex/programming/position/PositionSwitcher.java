package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.directionToCommand;

import java.util.Map;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.IEngine.OrderCommand;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.common.collect.ImmutableMap;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderParamsSupplier;
import com.jforex.programming.settings.UserSettings;

public final class PositionSwitcher {

    private final Position position;
    private final OrderParamsSupplier orderParamsSupplier;
    private String mergeLabel;
    private Map<PositionEvent, Runnable> positionEventActions;

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

    private final StateMachineConfig<FSMState, FSMTrigger> fsmConfig = new StateMachineConfig<>();
    private final StateMachine<FSMState, FSMTrigger> fsm = new StateMachine<>(FSMState.FLAT, fsmConfig);

    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);

    public PositionSwitcher(final Position position,
                            final OrderParamsSupplier orderParamsSupplier) {
        this.position = position;
        this.orderParamsSupplier = orderParamsSupplier;

        subscribeToPositionEvents();
        configureFSM();
    }

    private final void subscribeToPositionEvents() {
        position.positionEventObs()
                .subscribe(this::processPositionEvent);
    }

    private final void configureFSM() {
        positionEventActions = ImmutableMap.<PositionEvent, Runnable> builder()
                .put(PositionEvent.SUBMITTASK_DONE, () -> position.merge(mergeLabel))
                .put(PositionEvent.MERGETASK_DONE, () -> fsm.fire(FSMTrigger.MERGE_DONE))
                .put(PositionEvent.CLOSETASK_DONE, () -> fsm.fire(FSMTrigger.CLOSE_DONE))
                .build();

        fsmConfig.configure(FSMState.FLAT)
                .permit(FSMTrigger.BUY, FSMState.BUSY)
                .permit(FSMTrigger.SELL, FSMState.BUSY)
                .ignore(FSMTrigger.FLAT);

        fsmConfig.configure(FSMState.LONG)
                .permit(FSMTrigger.FLAT, FSMState.BUSY)
                .permit(FSMTrigger.SELL, FSMState.BUSY)
                .ignore(FSMTrigger.BUY);

        fsmConfig.configure(FSMState.SHORT)
                .permit(FSMTrigger.FLAT, FSMState.BUSY)
                .permit(FSMTrigger.BUY, FSMState.BUSY)
                .ignore(FSMTrigger.SELL);

        fsmConfig.configure(FSMState.BUSY)
                .onEntryFrom(FSMTrigger.BUY, () -> executeOrderCommandSignal(OrderDirection.LONG))
                .onEntryFrom(FSMTrigger.SELL, () -> executeOrderCommandSignal(OrderDirection.SHORT))
                .onEntryFrom(FSMTrigger.FLAT, () -> position.close())
                .permitDynamic(FSMTrigger.MERGE_DONE, () -> {
                    final OrderDirection orderDirection = position.direction();
                    if (orderDirection == OrderDirection.FLAT)
                        return FSMState.FLAT;
                    if (orderDirection == OrderDirection.LONG)
                        return FSMState.LONG;
                    return FSMState.SHORT;
                })
                .permit(FSMTrigger.CLOSE_DONE, FSMState.FLAT)
                .ignore(FSMTrigger.FLAT)
                .ignore(FSMTrigger.BUY)
                .ignore(FSMTrigger.SELL);
    }

    private final void processPositionEvent(final PositionEvent positonEvent) {
        positionEventActions.get(positonEvent).run();
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
        mergeLabel = userSettings.defaultMergePrefix() + adaptedOrderParams.label();
        position.submit(adaptedOrderParams);
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
