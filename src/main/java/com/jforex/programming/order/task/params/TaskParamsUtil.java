package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;

public class TaskParamsUtil {

    public void subscribeBasicParams(final Observable<OrderEvent> observable,
                                     final CommonParamsBase commonParamsBase) {
        subscribeComposeParams(composeEvents(observable, commonParamsBase.consumerForEvent()),
                               commonParamsBase.composeParams());
    }

    public void subscribeComposeParams(final Observable<OrderEvent> observable,
                                       final ComposeParams composeParams) {
        composeRetry(observable, composeParams.retryParams())
            .doOnSubscribe(d -> composeParams.startAction().run())
            .subscribe(orderEvent -> {},
                       composeParams.errorConsumer()::accept,
                       composeParams.completeAction());
    }

    public Observable<OrderEvent> composeEvents(final Observable<OrderEvent> observable,
                                                final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent) {
        return observable.doOnNext(orderEvent -> handlerOrderEvent(orderEvent, consumerForEvent));
    }

    public Observable<OrderEvent> composeRetry(final Observable<OrderEvent> observable,
                                               final RetryParams retryParams) {
        final int noOfRetries = retryParams.noOfRetries();
        return noOfRetries > 0
                ? observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, retryParams.delayInMillis()))
                : observable;
    }

    private void handlerOrderEvent(final OrderEvent orderEvent,
                                   final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent) {
        final OrderEventType type = orderEvent.type();
        if (consumerForEvent.containsKey(type))
            consumerForEvent.get(type).accept(orderEvent);
    }

    public Observable<OrderEvent> composeParams(final Observable<OrderEvent> observable,
                                                final ComposeParams composeParams) {
        if (observable == null)
            System.out.println("1111");

        return composeRetry(observable, composeParams.retryParams())
            .doOnSubscribe(d -> composeParams.startAction().run())
            .doOnComplete(composeParams.completeAction()::run)
            .doOnError(composeParams.errorConsumer()::accept);
    }

    public Observable<OrderEvent> composeParamsForOrder(final IOrder order,
                                                        final Observable<OrderEvent> observable,
                                                        final ComposeParamsForOrder composeParamsForOrder,
                                                        final Map<OrderEventType,
                                                                  Consumer<OrderEvent>> consumerForEvent) {
        return composeParams(composeEvents(observable, consumerForEvent),
                             composeParamsForOrder.convertWithOrder(order));
    }
}
