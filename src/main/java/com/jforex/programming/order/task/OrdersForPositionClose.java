package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.position.PositionUtil;

public class OrdersForPositionClose {

    private final PositionUtil positionUtil;

    public OrdersForPositionClose(final PositionUtil positionUtil) {
        this.positionUtil = positionUtil;
    }

    public Collection<IOrder> filled(final Instrument instrument) {
        return positionUtil.filledOrders(instrument);
    }

    public Collection<IOrder> forMode(final ClosePositionParams closePositionParams) {
        final Instrument instrument = closePositionParams.instrument();
        final CloseExecutionMode closeExecutionMode = closePositionParams.closeExecutionMode();

        if (closeExecutionMode == CloseExecutionMode.CloseFilled)
            return positionUtil.filledOrders(instrument);
        if (closeExecutionMode == CloseExecutionMode.CloseOpened)
            return positionUtil.openedOrders(instrument);
        return positionUtil.filledOrOpenedOrders(instrument);
    }
}
