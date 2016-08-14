package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeTPEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetTPCommand implements OrderChangeCommand<Double> {

    private IOrder orderToChangeTP;
    private double currentValue;
    private double newValue;
    private String valueName;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_TP;
    private static final OrderEventTypeData orderEventTypeData = changeTPEventTypeData;

    public SetTPCommand(final IOrder orderToChangeTP,
                        final double newTP) {
        this.orderToChangeTP = orderToChangeTP;
        callable = () -> {
            orderToChangeTP.setTakeProfitPrice(newTP);
            return orderToChangeTP;
        };
        currentValue = orderToChangeTP.getTakeProfitPrice();
        newValue = newTP;
        valueName = "TP";
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    @Override
    public final boolean filter() {
        return !isTPSetTo(newValue).test(orderToChangeTP);
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
        return orderToChangeTP;
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
