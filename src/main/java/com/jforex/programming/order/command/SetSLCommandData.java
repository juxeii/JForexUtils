package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetSLCommandData implements OrderChangeCommandData<Double> {

    private final IOrder orderToChangeSL;
    private final double newSL;
    private final Callable<IOrder> callable;

    private static final OrderEventTypeData orderEventTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_SL),
                                   EnumSet.of(CHANGE_SL_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    public SetSLCommandData(final IOrder orderToChangeSL,
                            final double newSL) {
        this.orderToChangeSL = orderToChangeSL;
        this.newSL = newSL;
        callable = () -> {
            orderToChangeSL.setStopLossPrice(newSL);
            return orderToChangeSL;
        };
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean isValueNotSet() {
        return !isSLSetTo(newSL).test(orderToChangeSL);
    }

    @Override
    public final OrderCallReason callReason() {
        return OrderCallReason.CHANGE_SL;
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }
}
