package com.jforex.programming.order.spec;

import java.util.Map;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;
import io.reactivex.functions.Action;

public abstract class SpecBase<B, S> {

    protected Observable<OrderEvent> observable;
    protected final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    protected final ErrorConsumer errorConsumer;
    protected Action completeAction;
    private final int noOfRetries;
    private final long delayInMillis;

    protected SpecBase(final SpecBuilderBase<B, S> specBuilderBase) {
        observable = specBuilderBase.observable;
        consumerForEvent = specBuilderBase.consumerForEvent;
        errorConsumer = specBuilderBase.errorConsumer;
        completeAction = specBuilderBase.completeAction;
        noOfRetries = specBuilderBase.noOfRetries;
        delayInMillis = specBuilderBase.delayInMillis;

        setupRetry();
        observable.subscribe(this::handleEvent,
                             errorConsumer::accept,
                             completeAction::run);
    }

    private void handleEvent(final OrderEvent orderEvent) {
        final OrderEventType type = orderEvent.type();
        if (consumerForEvent.containsKey(type))
            consumerForEvent
                .get(type)
                .accept(orderEvent);
    }

    private void setupRetry() {
        if (noOfRetries > 0)
            observable = observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, delayInMillis));
    }
}
