package com.jforex.programming.order.command;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetGTTCommand extends OrderChangeCommand<Long> {

    public SetGTTCommand(final IOrder orderToChangeGTT,
                         final long newGTT) {
        super(orderToChangeGTT,
              () -> orderToChangeGTT.setGoodTillTime(newGTT),
              OrderEventTypeData.changeGTTData,
              orderToChangeGTT.getGoodTillTime(),
              newGTT,
              "open price");
    }
}
