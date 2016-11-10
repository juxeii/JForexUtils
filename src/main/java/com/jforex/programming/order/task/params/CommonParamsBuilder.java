package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

@SuppressWarnings("unchecked")
public abstract class CommonParamsBuilder<T> {

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent = new HashMap<>();

    protected T setEventConsumer(final OrderEventType orderEventType,
                                 final Consumer<OrderEvent> consumer) {
        checkNotNull(consumer);

        consumerForEvent.put(orderEventType, consumer);
        return (T) this;
    }
}
