package com.jforex.programming.order.spec;

import java.util.Map;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;
import io.reactivex.functions.Action;

public class GenericSpecBase {

    protected final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    protected final ErrorConsumer errorConsumer;
    protected Action startAction;
    protected Action completeAction;
    protected final int noOfRetries;
    protected final long delayInMillis;

    protected GenericSpecBase(final BuilderBase<?> specBuilderBase) {
        consumerForEvent = specBuilderBase.consumerForEvent;
        errorConsumer = specBuilderBase.errorConsumer;
        startAction = specBuilderBase.startAction;
        completeAction = specBuilderBase.completeAction;
        noOfRetries = specBuilderBase.noOfRetries;
        delayInMillis = specBuilderBase.delayInMillis;
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
