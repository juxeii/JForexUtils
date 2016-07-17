package com.jforex.programming.order.command;

import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

public class SetSLCommand extends OrderChangeCommand<Double> {

    public SetSLCommand(final IOrder orderToChangeSL,
                        final double newSL) {
        super(orderToChangeSL, () -> orderToChangeSL.setStopLossPrice(newSL));

        orderEventTypeData = OrderEventTypeData.changeSLData;
        currentValue = orderToChangeSL.getStopLossPrice();
        newValue = newSL;
        valueName = "SL";
    }
}
