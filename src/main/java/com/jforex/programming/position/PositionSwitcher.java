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
    private boolean isBusy = false;

    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);

    public PositionSwitcher(final Position position,
                            final OrderParamsSupplier orderParamsSupplier) {
        this.position = position;
        this.orderParamsSupplier = orderParamsSupplier;

        position.positionEventObs()
                .subscribe(this::processPositionEvent);
    }

    public final void sendBuySignal() {
        sendSwitchSignal(OrderDirection.LONG);
    }

    public final void sendSellSignal() {
        sendSwitchSignal(OrderDirection.SHORT);
    }

    public final void sendFlatSignal() {
        if (!isDirection(OrderDirection.FLAT) && !isBusy)
            position.close();
    }

    private final void sendSwitchSignal(final OrderDirection desiredDirection) {
        if (canSwitchTo(desiredDirection))
            executeOrderCommandSignal(desiredDirection);
    }

    public final boolean canSwitchTo(final OrderDirection desiredDirection) {
        return isDirection(OrderDirection.FLAT) || !isDirection(desiredDirection)
                ? !isBusy
                : false;
    }

    private final synchronized void executeOrderCommandSignal(final OrderDirection desiredDirection) {
        final OrderCommand newOrderCommand = directionToCommand(desiredDirection);
        final OrderParams adaptedOrderParams = adaptedOrderParams(newOrderCommand);
        mergeLabel = userSettings.defaultMergePrefix() + adaptedOrderParams.label();
        position.submit(adaptedOrderParams);
        isBusy = true;
    }

    private void processPositionEvent(final PositionEvent positonEvent) {
        if (positonEvent == PositionEvent.SUBMITTASK_DONE)
            position.merge(mergeLabel);
        else if (positonEvent == PositionEvent.MERGETASK_DONE)
            isBusy = false;
    }

    private final OrderParams adaptedOrderParams(final OrderCommand newOrderCommand) {
        final double absPositionExposure = Math.abs(position.signedExposure());
        final OrderParams orderParams = orderParamsSupplier.forCommand(newOrderCommand);
        return orderParams.clone()
                .withOrderCommand(newOrderCommand)
                .withAmount(orderParams.amount() + absPositionExposure)
                .build();
    }

    private final boolean isDirection(final OrderDirection direction) {
        return position.direction() == direction;
    }
}
