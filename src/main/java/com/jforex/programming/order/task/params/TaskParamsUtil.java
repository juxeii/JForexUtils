package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;

public class TaskParamsUtil {

    public void subscribeBasicParams(final Observable<OrderEvent> observable,
                                     final CommonParamsBase commonParamsBase) {
        subscribeComposeData(composeEvents(observable, commonParamsBase.consumerForEvent()),
                             commonParamsBase.composeData());
    }

    public void subscribeComposeData(final Observable<OrderEvent> observable,
                                     final ComposeData composeData) {
        composeRetry(observable, composeData.retryParams())
            .doOnSubscribe(d -> composeData.startAction().run())
            .subscribe(orderEvent -> {},
                       composeData.errorConsumer()::accept,
                       composeData.completeAction());
    }

    public Observable<OrderEvent> composeEvents(final Observable<OrderEvent> observable,
                                                final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent) {
        return observable.doOnNext(orderEvent -> handlerOrderEvent(orderEvent, consumerForEvent));
    }

    public Observable<OrderEvent> composeRetry(final Observable<OrderEvent> observable,
                                               final RetryParams retryParams) {
        final int noOfRetries = retryParams.noOfRetries();
        return noOfRetries > 0
                ? observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, retryParams.delayFunction()))
                : observable;
    }

    private void handlerOrderEvent(final OrderEvent orderEvent,
                                   final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent) {
        final OrderEventType type = orderEvent.type();
        if (consumerForEvent.containsKey(type))
            consumerForEvent.get(type).accept(orderEvent);
    }

    public Observable<OrderEvent> composeParams(final Observable<OrderEvent> observable,
                                                final ComposeData composeData) {
        return composeRetry(observable, composeData.retryParams())
            .doOnSubscribe(d -> composeData.startAction().run())
            .doOnComplete(composeData.completeAction()::run)
            .doOnError(composeData.errorConsumer()::accept);
    }

    public Observable<OrderEvent> composeParamsWithEvents(final Observable<OrderEvent> observable,
                                                          final ComposeData composeData,
                                                          final Map<OrderEventType,
                                                                    Consumer<OrderEvent>> consumerForEvent) {
        return composeParams(composeEvents(observable, consumerForEvent),
                             composeData);
    }
}
