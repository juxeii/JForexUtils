package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeGTTEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetGTTCommand implements OrderChangeCommand<Long> {

    private IOrder orderToChange;
    private Long currentValue;
    private Long newValue;
    private String valueName;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_GTT;
    private static final OrderEventTypeData orderEventTypeData = changeGTTEventTypeData;

    public SetGTTCommand(final IOrder orderToChangeGTT,
                         final long newGTT) {
        orderToChange = orderToChangeGTT;
        callable = () -> {
            orderToChangeGTT.setGoodTillTime(newGTT);
            return orderToChangeGTT;
        };
        currentValue = orderToChangeGTT.getGoodTillTime();
        newValue = newGTT;
        valueName = "open price";
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    @Override
    public final boolean filter() {
        return !isGTTSetTo(newValue).test(orderToChange);
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
        return orderToChange;
    }

    @Override
    public Long currentValue() {
        return currentValue;
    }

    @Override
    public Long newValue() {
        return newValue;
    }

    @Override
    public String valueName() {
        return valueName;
    }
}
