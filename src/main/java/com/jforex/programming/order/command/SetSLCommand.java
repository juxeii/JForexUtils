package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;

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

    @Override
    public boolean filter() {
        return !isSLSetTo(newValue).test(orderToChange);
    }
}
