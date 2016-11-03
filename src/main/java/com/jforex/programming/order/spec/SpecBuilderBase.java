package com.jforex.programming.order.spec;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;
import io.reactivex.functions.Action;

public abstract class SpecBuilderBase<B, S> {

    protected final Observable<OrderEvent> observable;
    protected ErrorConsumer errorConsumer = t -> {};
    protected Action completeAction = () -> {};
    protected final Map<OrderEventType, OrderEventConsumer> consumerForEvent = new HashMap<>();
    protected int noOfRetries;
    protected long delayInMillis;

    public SpecBuilderBase(final Observable<OrderEvent> observable) {
        this.observable = observable;
    }

    public SpecBuilderBase doOnException(final ErrorConsumer errorConsumer) {
        checkNotNull(errorConsumer);

        this.errorConsumer = errorConsumer;
        return this;
    }

    @SuppressWarnings("unchecked")
    public B doOnComplete(final Action completeAction) {
        checkNotNull(completeAction);

        this.completeAction = completeAction;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B retryOnReject(final int noOfRetries,
                           final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    protected B setEventConsumer(final OrderEventType orderEventType,
                                 final OrderEventConsumer consumer) {
        checkNotNull(consumer);

        consumerForEvent.put(orderEventType, consumer);
        return (B) this;
    }

    public abstract S start();
}
