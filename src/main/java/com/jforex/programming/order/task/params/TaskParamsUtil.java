package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;
import com.jforex.programming.order.task.params.basic.BasicParamsBase;

import io.reactivex.Observable;

public class TaskParamsUtil {

    public void subscribeBasicParams(final Observable<OrderEvent> observable,
                                     final BasicParamsBase basicParamsBase) {
        composeRetry(composeEventHandling(observable, basicParamsBase.consumerForEvent()), basicParamsBase)
            .doOnSubscribe(d -> basicParamsBase.startAction().run())
            .subscribe(orderEvent -> {},
                       basicParamsBase.errorConsumer()::accept,
                       basicParamsBase.completeAction()::run);
    }

    public Observable<OrderEvent> composeEventHandling(final Observable<OrderEvent> observable,
                                                       final Map<OrderEventType,
                                                                 Consumer<OrderEvent>> consumerForEvent) {
        return observable.doOnNext(orderEvent -> handlerOrderEvent(orderEvent, consumerForEvent));
    }

    private Observable<OrderEvent> composeRetry(final Observable<OrderEvent> observable,
                                                final CommonParamsBase commonParamsBase) {
        final int noOfRetries = commonParamsBase.noOfRetries();
        return noOfRetries > 0
                ? observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, commonParamsBase.delayInMillis()))
                : observable;
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

    public Observable<OrderEvent> composeTask(final Observable<OrderEvent> observable,
                                              final BasicParamsBase basicParamsBase) {
        return composeRetry(observable, basicParamsBase)
            .doOnSubscribe(d -> basicParamsBase.startAction().run())
            .doOnComplete(() -> basicParamsBase.completeAction().run())
            .doOnError(basicParamsBase.errorConsumer()::accept);
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
                                                        final ComposeParamsForOrder composeParams) {
        return composeRetry(observable, composeParams.retryParams())
            .doOnSubscribe(d -> composeParams.startAction(order).run())
            .doOnComplete(() -> composeParams.completeAction(order).run())
            .doOnError(composeParams.errorConsumer(order)::accept);
    }

    public void subscribeComposeParams(final Observable<OrderEvent> observable,
                                       final ComposeParams composeParams) {
        composeRetry(observable, composeParams.retryParams())
            .doOnSubscribe(d -> composeParams.startAction().run())
            .subscribe(orderEvent -> {},
                       composeParams.errorConsumer()::accept,
                       composeParams.completeAction());
    }

    public Observable<OrderEvent> composeEventHandling(final Observable<OrderEvent> observable,
                                                       final BasicParamsBase basicParamsBase) {
        return composeTask(composeEventHandling(observable, basicParamsBase.consumerForEvent()),
                           basicParamsBase);
    }

    public void subscribePositionTask(final Observable<OrderEvent> observable,
                                      final BasicParamsBase basicParamsBase) {
        composeRetry(observable, basicParamsBase)
            .doOnSubscribe(d -> basicParamsBase.startAction().run())
            .subscribe(orderEvent -> {},
                       basicParamsBase.errorConsumer()::accept,
                       basicParamsBase.completeAction()::run);
    }
}
