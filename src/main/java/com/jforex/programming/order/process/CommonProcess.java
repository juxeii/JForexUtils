package com.jforex.programming.order.process;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import rx.functions.Action0;

public class CommonProcess {

    private final Action0 completedAction;
    private final Consumer<OrderEvent> eventAction;
    private final Consumer<Throwable> errorAction;
    private final int noOfRetries;
    private final long delayInMillis;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    protected CommonProcess(final CommonBuilder<?> builder) {
        completedAction = builder.completedAction;
        eventAction = builder.eventAction;
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
        eventHandlerForType = builder.eventHandlerForType;
    }

    public final Action0 completedAction() {
        return completedAction;
    }

    public final Consumer<OrderEvent> eventAction() {
        return eventAction;
    }

    public final Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public final int noOfRetries() {
        return noOfRetries;
    }

    public final long delayInMillis() {
        return delayInMillis;
    }

    public final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType() {
        return eventHandlerForType;
    }
}
