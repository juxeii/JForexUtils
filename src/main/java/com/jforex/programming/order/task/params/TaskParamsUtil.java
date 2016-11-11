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
        final ComposeParams composeParams = commonParamsBase.composeParams();
        composeRetry(composeEventHandling(observable, commonParamsBase.consumerForEvent()),
                     composeParams.retryParams())
                         .doOnSubscribe(d -> composeParams.startAction().run())
                         .subscribe(orderEvent -> {},
                                    composeParams.errorConsumer()::accept,
                                    composeParams.completeAction()::run);
    }

    public Observable<OrderEvent> composeEventHandling(final Observable<OrderEvent> observable,
                                                       final Map<OrderEventType,
                                                                 Consumer<OrderEvent>> consumerForEvent) {
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
        return composeRetry(observable, composeParams.retryParams())
            .doOnSubscribe(d -> composeParams.startAction().run())
            .doOnComplete(() -> composeParams.completeAction().run())
            .doOnError(composeParams.errorConsumer()::accept);
    }

    public Observable<OrderEvent> composeParamsForOrder(final IOrder order,
                                                        final Observable<OrderEvent> observable,
                                                        final ComposeParamsForOrder composeParams,
                                                        final Map<OrderEventType,
                                                                  Consumer<OrderEvent>> consumerForEvent) {
        final Observable<OrderEvent> withEventObservable = composeEventHandling(observable, consumerForEvent);
        return composeRetry(withEventObservable, composeParams.retryParams())
            .doOnSubscribe(d -> composeParams.startAction(order).run())
            .doOnComplete(() -> composeParams.completeAction(order).run())
            .doOnError(composeParams.errorConsumer(order)::accept);
    }

    public Observable<OrderEvent> composeEventHandling(final Observable<OrderEvent> observable,
                                                       final CommonParamsBase commonParamsBase) {
        return composeParams(composeEventHandling(observable, commonParamsBase.consumerForEvent()),
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
}
