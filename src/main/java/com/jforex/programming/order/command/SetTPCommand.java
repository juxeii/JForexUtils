package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetTPCommand extends OrderChangeCommand<Double> {

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
    public final boolean filter() {
        return !isTPSetTo(newValue).test(orderToChange);
    }

    @Override
    public OrderCallReason callReason() {
        return OrderCallReason.CHANGE_TP;
    }
}
