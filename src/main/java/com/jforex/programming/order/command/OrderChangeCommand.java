package com.jforex.programming.order.command;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.event.OrderEventTypeData;

public class OrderChangeCommand<T> extends OrderCallCommand {

    private final String commonLog;

    public OrderChangeCommand(final IOrder orderToChange,
                              final JFRunnable runnable,
                              final OrderEventTypeData orderEventTypeData,
                              final T currentValue,
                              final T newValue,
                              final String valueName) {
        this.orderEventTypeData = orderEventTypeData;
        callable = OrderStaticUtil.runnableToCallable(runnable, orderToChange);
        commonLog = valueName + " from " + currentValue + " to " + newValue + " for order "
                + orderToChange.getLabel() + " and instrument " + orderToChange.getInstrument();
    }

    @Override
    protected String subscribeLog() {
        return "Start to change " + commonLog;
    }

    @Override
    protected String errorLog(final Throwable e) {
        return "Failed to change " + commonLog + "!Excpetion: " + e.getMessage();
    }

    @Override
    protected String completedLog() {
        return "Changed " + commonLog;
    }
}
