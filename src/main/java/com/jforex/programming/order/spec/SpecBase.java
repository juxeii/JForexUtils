package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;

public class SpecBase extends GenericSpecBase {

    protected Observable<OrderEvent> observable;

    protected SpecBase(final SpecBuilderBase<?> specBuilderBase) {
        super(specBuilderBase);

        observable = specBuilderBase.observable;
    }

    public Observable<OrderEvent> observable() {
        return observable;
    }

    protected void composeObservable() {
        setupRetry();
        setUpEventHandlers();
    }

    protected void subscribe() {
        setupRetry();
        setUpEventHandlersForSubscription();
    }

    protected void setupRetry() {
        if (noOfRetries > 0)
            observable = observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, delayInMillis));
    }

    protected void setUpEventHandlers() {
        observable = observable
            .doOnSubscribe(d -> startAction.run())
            .doOnComplete(completeAction)
            .doOnError(errorConsumer::accept)
            .doOnNext(this::handleEvent);
    }

    protected void setUpEventHandlersForSubscription() {
        observable
            .doOnSubscribe(d -> startAction.run())
            .subscribe(this::handleEvent,
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
}
