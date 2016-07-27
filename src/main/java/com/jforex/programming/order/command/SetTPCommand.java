package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetTPCommand extends OrderChangeCommand<Double> {

    public SetTPCommand(final IOrder orderToChangeTP,
                        final double newTP) {
        super(orderToChangeTP,
              () -> orderToChangeTP.setTakeProfitPrice(newTP),
              OrderEventTypeData.changeTPData,
              orderToChangeTP.getTakeProfitPrice(),
              newTP,
              "TP");
    }

    @Override
    public boolean filter(final IOrder order) {
        return !isTPSetTo(newValue).test(order);
    }
}