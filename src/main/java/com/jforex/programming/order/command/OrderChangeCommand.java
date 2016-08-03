package com.jforex.programming.order.command;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.event.OrderEventTypeData;

public abstract class OrderChangeCommand<T> extends OrderCallCommand {

    protected IOrder orderToChange;
    protected T newValue;
    private final String commonLog;

    public OrderChangeCommand(final IOrder orderToChange,
                              final JFRunnable runnable,
                              final OrderEventTypeData orderEventTypeData,
                              final T currentValue,
                              final T newValue,
                              final String valueName) {
        super(OrderStaticUtil.runnableToCallable(runnable, orderToChange),
              orderEventTypeData);

        this.orderToChange = orderToChange;
        this.newValue = newValue;

        commonLog = valueName + " from " + currentValue + " to " + newValue + " for order "
                + orderToChange.getLabel() + " and instrument " + orderToChange.getInstrument();
    }

    public abstract boolean filter();

    @Override
    protected final String subscribeLog() {
        return "Start to change " + commonLog;
    }

    @Override
    protected final String errorLog(final Throwable t) {
        return "Failed to change " + commonLog + "!Excpetion: " + t.getMessage();
    }

    @Override
    protected final String completedLog() {
        return "Changed " + commonLog;
    }
}
