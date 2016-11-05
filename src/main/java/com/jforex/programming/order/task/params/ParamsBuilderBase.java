package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.functions.Action;

@SuppressWarnings("unchecked")
public class ParamsBuilderBase<B> {

    public Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    public ErrorConsumer errorConsumer;
    public Action startAction;
    public Action completeAction;
    public int noOfRetries;
    public long delayInMillis;

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
