package com.jforex.programming.order.command;

import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

public class SetOpenPriceCommand extends OrderChangeCommand<Double> {

    public SetOpenPriceCommand(final IOrder orderToChangeOpenPrice,
                               final double newOpenPrice) {
        super(orderToChangeOpenPrice, () -> orderToChangeOpenPrice.setOpenPrice(newOpenPrice));

        orderEventTypeData = OrderEventTypeData.changeOpenPriceData;
        currentValue = orderToChangeOpenPrice.getOpenPrice();
        newValue = newOpenPrice;
        valueName = "open price";
    }
}
