package com.jforex.programming.order.command;

import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

public class SetGTTCommand extends OrderChangeCommand<Long> {

    public SetGTTCommand(final IOrder orderToChangeGTT,
                         final long newGTT) {
        super(orderToChangeGTT, () -> orderToChangeGTT.setGoodTillTime(newGTT));

        orderEventTypeData = OrderEventTypeData.changeGTTData;
        currentValue = orderToChangeGTT.getGoodTillTime();
        newValue = newGTT;
        valueName = "open price";
    }
}
