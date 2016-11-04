package com.jforex.programming.order.spec;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.functions.Action;

@SuppressWarnings("unchecked")
public class ObservableComposerBuilder<B> {

    protected ErrorConsumer errorConsumer = t -> {};
    protected Action startAction = () -> {};
    protected Action completeAction = () -> {};
    protected final Map<OrderEventType, OrderEventConsumer> consumerForEvent = new HashMap<>();
    protected int noOfRetries;
    protected long delayInMillis;

    public B doOnStart(final Action startAction) {
        checkNotNull(startAction);

        this.startAction = startAction;
        return (B) this;
    }

    public B doOnException(final ErrorConsumer errorConsumer) {
        checkNotNull(errorConsumer);

        this.errorConsumer = errorConsumer;
        return (B) this;
    }

    public B doOnComplete(final Action completeAction) {
        checkNotNull(completeAction);

        this.completeAction = completeAction;
        return (B) this;
    }

    public B retryOnReject(final int noOfRetries,
                           final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
        return (B) this;
    }

    protected B setEventConsumer(final OrderEventType orderEventType,
                                 final OrderEventConsumer consumer) {
        checkNotNull(consumer);

        consumerForEvent.put(orderEventType, consumer);
        return (B) this;
    }
}
