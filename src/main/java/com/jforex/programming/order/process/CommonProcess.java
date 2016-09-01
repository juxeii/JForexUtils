package com.jforex.programming.order.process;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class CommonProcess {

    private final Consumer<Throwable> errorAction;
    private final int noOfRetries;
    private final long delayInMillis;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    protected CommonProcess(final CommonBuilder<?> builder) {
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
        eventHandlerForType = builder.eventHandlerForType;
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
