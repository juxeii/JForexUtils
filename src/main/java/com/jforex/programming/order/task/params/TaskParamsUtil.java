package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;

public class TaskParamsUtil {

    public void composeAndSubscribe(final Observable<OrderEvent> observable,
                                    final TaskParamsBase taskParams) {
        final ComposeData composeData = taskParams.composeData();
        composeRetry(composeEvents(observable, taskParams), composeData.retryParams())
            .doOnSubscribe(d -> composeData.startAction().run())
            .subscribe(orderEvent -> {},
                       composeData.errorConsumer()::accept,
                       composeData.completeAction());
    }

    private Observable<OrderEvent> composeEvents(final Observable<OrderEvent> observable,
                                                 final TaskParamsBase taskParams) {
        final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent = taskParams
            .composeData()
            .consumerByEventType();
        return observable.doOnNext(orderEvent -> handlerOrderEvent(orderEvent, consumerForEvent));
    }

    private Observable<OrderEvent> composeRetry(final Observable<OrderEvent> observable,
                                                final RetryParams retryParams) {
        final int noOfRetries = retryParams.noOfRetries();
        return noOfRetries > 0
                ? TaskRetry.rejectObservable(observable, retryParams)
                : observable;
    }

    private void handlerOrderEvent(final OrderEvent orderEvent,
                                   final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent) {
        final OrderEventType type = orderEvent.type();
        if (consumerForEvent.containsKey(type))
            consumerForEvent.get(type).accept(orderEvent);
    }

    public Observable<OrderEvent> compose(final Observable<OrderEvent> observable,
                                          final TaskParamsBase taskParams) {
        final ComposeData composeData = taskParams.composeData();
        return composeRetry(composeEvents(observable, taskParams), composeData.retryParams())
            .doOnSubscribe(d -> composeData.startAction().run())
            .doOnComplete(composeData.completeAction()::run)
            .doOnError(composeData.errorConsumer()::accept);
    }
}
