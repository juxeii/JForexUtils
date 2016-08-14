package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeSLEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetSLCommand implements OrderChangeCommand<Double> {

    private IOrder orderToChangeSL;
    private double currentValue;
    private double newValue;
    private String valueName;
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
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    @Override
    public final boolean filter() {
        return !isSLSetTo(newValue).test(orderToChangeSL);
    }

    @Override
    public Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public OrderCallReason callReason() {
        return callReason;
    }

    @Override
    public IOrder order() {
        return orderToChangeSL;
    }

    @Override
    public Double currentValue() {
        return currentValue;
    }

    @Override
    public Double newValue() {
        return newValue;
    }

    @Override
    public String valueName() {
        return valueName;
    }
}
