package com.jforex.programming.order.command;

import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

public class CloseCommand extends OrderChangeCommand<IOrder.State> {

    public CloseCommand(final IOrder orderToClose) {
        super(orderToClose, () -> orderToClose.close());

        orderEventTypeData = OrderEventTypeData.closeData;
        currentValue = orderToClose.getState();
        newValue = IOrder.State.CLOSED;
        valueName = "order state";
    }
}
