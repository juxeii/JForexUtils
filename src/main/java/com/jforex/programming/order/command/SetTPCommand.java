package com.jforex.programming.order.command;

import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

public class SetTPCommand extends OrderChangeCommand<Double> {

    public SetTPCommand(final IOrder orderToChangeTP,
                        final double newTP) {
        super(orderToChangeTP, () -> orderToChangeTP.setTakeProfitPrice(newTP));

        orderEventTypeData = OrderEventTypeData.changeTPData;
        currentValue = orderToChangeTP.getTakeProfitPrice();
        newValue = newTP;
        valueName = "TP";
    }
}
