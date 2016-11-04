package com.jforex.programming.order.spec;

import java.util.Map;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;
import io.reactivex.functions.Action;

public abstract class ObservableComposer {

    protected final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    protected final ErrorConsumer errorConsumer;
    protected Action startAction;
    protected Action completeAction;
    protected final int noOfRetries;
    protected final long delayInMillis;

    protected ObservableComposer(final ObservableComposerBuilder<?> builder) {
        consumerForEvent = builder.consumerForEvent;
        errorConsumer = builder.errorConsumer;
        startAction = builder.startAction;
        completeAction = builder.completeAction;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
    }

    public Observable<OrderEvent> compose(final Observable<OrderEvent> observable) {
        return setupRetry(observable)
            .doOnSubscribe(d -> startAction.run())
            .doOnComplete(completeAction)
            .doOnError(errorConsumer::accept)
            .doOnNext(this::handleEvent);
    }

    private Observable<OrderEvent> setupRetry(final Observable<OrderEvent> observable) {
        return noOfRetries > 0
                ? observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, delayInMillis))
                : observable;
    }

    private void handleEvent(final OrderEvent orderEvent) {
        final OrderEventType type = orderEvent.type();
        if (consumerForEvent.containsKey(type))
            consumerForEvent
                .get(type)
                .accept(orderEvent);
    }
}
