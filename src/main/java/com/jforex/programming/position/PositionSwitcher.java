package com.jforex.programming.position;

import static com.jforex.programming.misc.JForexUtil.uss;
import static com.jforex.programming.order.OrderStaticUtil.directionToCommand;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderParamsSupplier;

public final class PositionSwitcher {

    private final Position position;
    private final OrderParamsSupplier orderParamsSupplier;

    public PositionSwitcher(final Position position,
                            final OrderParamsSupplier orderParamsSupplier) {
        this.position = position;
        this.orderParamsSupplier = orderParamsSupplier;
    }

    public final void sendBuySignal() {
        sendSwitchSignal(OrderDirection.LONG);
    }

    public final void sendSellSignal() {
        sendSwitchSignal(OrderDirection.SHORT);
    }

    public final void sendFlatSignal() {
        if (!isDirection(OrderDirection.FLAT) && !isBusy())
            position.close();
    }

    private final void sendSwitchSignal(final OrderDirection desiredDirection) {
        if (canSwitchTo(desiredDirection))
            executeOrderCommandSignal(desiredDirection);
    }

    public final boolean canSwitchTo(final OrderDirection desiredDirection) {
        return isDirection(OrderDirection.FLAT) || !isDirection(desiredDirection)
                ? !isBusy()
                : false;
    }

    private final void executeOrderCommandSignal(final OrderDirection desiredDirection) {
        final OrderCommand newOrderCommand = directionToCommand(desiredDirection);
        final OrderParams adaptedOrderParams = adaptedOrderParams(newOrderCommand);
        final String mergeLabel = uss.ORDER_MERGE_LABEL_PREFIX() + adaptedOrderParams.label();
        position.submit(adaptedOrderParams);
        if (position.filter(isFilled).size() >= 1)
            position.merge(mergeLabel);
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

    private final boolean isBusy() {
        return position.isBusy();
    }
}
