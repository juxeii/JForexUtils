package com.jforex.programming.order.command;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetSLCommand extends OrderChangeCommand<Double> {

    public SetSLCommand(final IOrder orderToChangeSL,
                        final double newSL) {
        super(orderToChangeSL,
              () -> orderToChangeSL.setStopLossPrice(newSL),
              OrderEventTypeData.changeSLData,
              orderToChangeSL.getStopLossPrice(),
              newSL,
              "SL");
    }
}
