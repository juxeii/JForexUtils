package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.event.OrderEventTypeData.closeEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class CloseCommand implements OrderChangeCommand<IOrder.State> {

    private IOrder orderToClose;
    private IOrder.State currentValue;
    private IOrder.State newValue;
    private String valueName;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CLOSE;
    private static final OrderEventTypeData orderEventTypeData = closeEventTypeData;

    public CloseCommand(final IOrder orderToClose) {
        this.orderToClose = orderToClose;
        callable = () -> {
            orderToClose.close();
            return orderToClose;
        };
        currentValue = orderToClose.getState();
        newValue = IOrder.State.CLOSED;
        valueName = "open price";
    }

    @Override
    public final boolean filter() {
        return !isClosed.test(orderToClose);
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
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
        return orderToClose;
    }

    @Override
    public IOrder.State currentValue() {
        return currentValue;
    }

    @Override
    public IOrder.State newValue() {
        return newValue;
    }

    @Override
    public String valueName() {
        return valueName;
    }
}
