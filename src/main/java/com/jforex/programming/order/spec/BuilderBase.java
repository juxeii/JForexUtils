package com.jforex.programming.order.spec;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;
import io.reactivex.functions.Action;

public class BuilderBase<B> {

    protected ErrorConsumer errorConsumer = t -> {};
    protected Action startAction = () -> {};
    protected Action completeAction = () -> {};
    protected final Map<OrderEventType, OrderEventConsumer> consumerForEvent = new HashMap<>();
    protected int noOfRetries;
    protected long delayInMillis;

    @SuppressWarnings("unchecked")
    public B doOnStart(final Action startAction) {
        checkNotNull(startAction);

        this.startAction = startAction;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B doOnException(final ErrorConsumer errorConsumer) {
        checkNotNull(errorConsumer);

        this.errorConsumer = errorConsumer;
        return (B) this;
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

    protected Observable<OrderEvent> composeObservable(Observable<OrderEvent> observable) {
        if (noOfRetries > 0)
            observable = observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, delayInMillis));
        return observable
            .doOnSubscribe(d -> startAction.run())
            .doOnComplete(completeAction)
            .doOnError(errorConsumer::accept)
            .doOnNext(orderEvent -> {
                final OrderEventType type = orderEvent.type();
                if (consumerForEvent.containsKey(type))
                    consumerForEvent
                        .get(type)
                        .accept(orderEvent);
            });
    }
}
