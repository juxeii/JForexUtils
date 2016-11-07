package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.functions.Action;

@SuppressWarnings("unchecked")
public class BasicParamsBuilder<T> {

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent = new HashMap<>();
    public Consumer<Throwable> errorConsumer;
    public Action startAction;
    public Action completeAction;
    public int noOfRetries;
    public long delayInMillis;

    public T doOnStart(final Action startAction) {
        checkNotNull(startAction);

        this.startAction = startAction;
        return (T) this;
    }

    public T doOnException(final Consumer<Throwable> errorConsumer) {
        checkNotNull(errorConsumer);

        this.errorConsumer = errorConsumer;
        return (T) this;
    }

    public T doOnComplete(final Action completeAction) {
        checkNotNull(completeAction);

        this.completeAction = completeAction;
        return (T) this;
    }

    public T retryOnReject(final int noOfRetries,
                           final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
        return (T) this;
    }

    protected T setEventConsumer(final OrderEventType orderEventType,
                                 final Consumer<OrderEvent> consumer) {
        checkNotNull(consumer);

        consumerForEvent.put(orderEventType, consumer);
        return (T) this;
    }
}
