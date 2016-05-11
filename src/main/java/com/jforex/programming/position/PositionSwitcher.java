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
import com.jforex.programming.position.test.PositionSubmitAndMerge;
import com.jforex.programming.settings.UserSettings;

public final class PositionSwitcher {

    private final Position position;
    private final PositionSubmitAndMerge positionSubmitAndMerge;
    private final OrderParamsSupplier orderParamsSupplier;
    private Map<PositionEvent, Runnable> positionEventActions;
    private Map<OrderDirection, FSMState> nextStatesByDirection;

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
        positionSubmitAndMerge = new PositionSubmitAndMerge(position);

        configurePositionEventActions();
        configureFSM();
        subscribeToPositionEvents();
    }

    private final void configurePositionEventActions() {
        positionEventActions = ImmutableMap.<PositionEvent, Runnable> builder()
                .put(PositionEvent.MERGETASK_DONE, () -> fsm.fire(FSMTrigger.MERGE_DONE))
                .put(PositionEvent.CLOSETASK_DONE, () -> fsm.fire(FSMTrigger.CLOSE_DONE))
                .build();
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
                .permitDynamic(FSMTrigger.MERGE_DONE, () -> nextStatesByDirection.get(position.direction()))
                .permit(FSMTrigger.CLOSE_DONE, FSMState.FLAT)
                .ignore(FSMTrigger.FLAT)
                .ignore(FSMTrigger.BUY)
                .ignore(FSMTrigger.SELL);
    }

    private final void subscribeToPositionEvents() {
        position.positionEventObs()
                .filter(positonEvent -> positonEvent == PositionEvent.MERGETASK_DONE
                        || positonEvent == PositionEvent.CLOSETASK_DONE)
                .subscribe(positonEvent -> positionEventActions.get(positonEvent).run());
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
        final String mergeLabel = userSettings.defaultMergePrefix() + adaptedOrderParams.label();
        positionSubmitAndMerge.submitAndMerge(adaptedOrderParams, mergeLabel);
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
