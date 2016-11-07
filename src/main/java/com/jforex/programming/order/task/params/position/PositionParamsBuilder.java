package com.jforex.programming.order.task.params.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

@SuppressWarnings("unchecked")
public class PositionParamsBuilder<T, V> {

    protected Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;
    protected BiConsumer<Throwable, V> errorConsumer;
    protected Consumer<V> startConsumer;
    protected Consumer<V> completeConsumer;
    protected int noOfRetries;
    protected long delayInMillis;

    protected T setEventConsumer(final OrderEventType orderEventType,
                                 final Consumer<OrderEvent> consumer) {
        checkNotNull(consumer);

        consumerForEvent.put(orderEventType, consumer);
        return (T) this;
    }

    public T doOnStart(final Consumer<V> startConsumer) {
        checkNotNull(startConsumer);

        this.startConsumer = startConsumer;
        return (T) this;
    }

    public T doOnException(final BiConsumer<Throwable, V> errorConsumer) {
        checkNotNull(errorConsumer);

        this.errorConsumer = errorConsumer;
        return (T) this;
    }

    public T doOnComplete(final Consumer<V> completeConsumer) {
        checkNotNull(completeConsumer);

        this.completeConsumer = completeConsumer;
        return (T) this;
    }

    public T retryOnReject(final int noOfRetries,
                           final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
        return (T) this;
    }
}
