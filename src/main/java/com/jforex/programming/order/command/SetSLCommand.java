package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeSLEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetSLCommand implements OrderChangeCommand<Double> {

    private final IOrder orderToChangeSL;
    private final double newSL;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_SL;
    private static final OrderEventTypeData orderEventTypeData = changeSLEventTypeData;

    public SetSLCommand(final IOrder orderToChangeSL,
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
    public final boolean filter() {
        return !isSLSetTo(newSL).test(orderToChangeSL);
    }

    @Override
    public final OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    @Override
    public final OrderCallReason callReason() {
        return callReason;
    }
}
