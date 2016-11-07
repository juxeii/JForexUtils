package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public abstract class CommonParamsBase {

    protected Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;
    private final int noOfRetries;
    private final long delayInMillis;

    protected CommonParamsBase(final CommonParamsBuilder<?> builder) {
        consumerForEvent = builder.consumerForEvent;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    public int noOfRetries() {
        return noOfRetries;
    }

    public long delayInMillis() {
        return delayInMillis;
    }
}
