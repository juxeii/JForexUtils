package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeTPEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetTPCommand implements OrderChangeCommand<Double> {

    private final IOrder orderToChangeTP;
    private final double currentValue;
    private final double newValue;
    private final String valueName;
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
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean filter() {
        return !isTPSetTo(newValue).test(orderToChangeTP);
    }

    @Override
    public final IOrder order() {
        return orderToChangeTP;
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
