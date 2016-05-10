package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.directionToCommand;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderParamsSupplier;
import com.jforex.programming.settings.UserSettings;

public final class PositionSwitcher {

    private final Position position;
    private final OrderParamsSupplier orderParamsSupplier;
    private String mergeLabel;
    private PositionSwitcherFSM fsm = PositionSwitcherFSM.FLAT;

    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);

    public PositionSwitcher(final Position position,
                            final OrderParamsSupplier orderParamsSupplier) {
        this.position = position;
        this.orderParamsSupplier = orderParamsSupplier;

        position.positionEventObs()
                .subscribe(this::processPositionEvent);
    }

    private void processPositionEvent(final PositionEvent positonEvent) {
        if (positonEvent == PositionEvent.SUBMITTASK_DONE)
            fsm = fsm.triggerSubmitDone(this);
        else if (positonEvent == PositionEvent.MERGETASK_DONE)
            fsm = fsm.triggerMergeDone(position.direction());
        else if (positonEvent == PositionEvent.CLOSETASK_DONE)
            fsm = fsm.triggerCloseDone();
    }

    public final void sendBuySignal() {
        fsm = fsm.sendBuySignal(this);
    }

    public final void sendSellSignal() {
        fsm = fsm.sendSellSignal(this);
    }

    public final void sendFlatSignal() {
        fsm = fsm.sendFlatSignal(this);
    }

    protected void startMerge() {
        position.merge(mergeLabel);
    }

    protected void startClose() {
        position.close();
    }

    protected final void executeOrderCommandSignal(final OrderDirection desiredDirection) {
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
