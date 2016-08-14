package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeSLEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetSLCommand implements OrderChangeCommand<Double> {

    private final IOrder orderToChangeSL;
    private final double currentValue;
    private final double newValue;
    private final String valueName;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_SL;
    private static final OrderEventTypeData orderEventTypeData = changeSLEventTypeData;

    public SetSLCommand(final IOrder orderToChangeSL,
                        final double newSL) {
        this.orderToChangeSL = orderToChangeSL;
        callable = () -> {
            orderToChangeSL.setStopLossPrice(newSL);
            return orderToChangeSL;
        };
        currentValue = orderToChangeSL.getStopLossPrice();
        newValue = newSL;
        valueName = "SL";
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean filter() {
        return !isSLSetTo(newValue).test(orderToChangeSL);
    }

    @Override
    public final IOrder order() {
        return orderToChangeSL;
    }

    @Override
    public final Double currentValue() {
        return currentValue;
    }

    @Override
    public final Double newValue() {
        return newValue;
    }

    @Override
    public final String valueName() {
        return valueName;
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
