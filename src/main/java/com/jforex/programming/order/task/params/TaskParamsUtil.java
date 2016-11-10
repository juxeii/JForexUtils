package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;
import com.jforex.programming.order.task.params.basic.BasicParamsBase;

import io.reactivex.Observable;

public class TaskParamsUtil {

    public void subscribeBasicParams(final Observable<OrderEvent> observable,
                                     final BasicParamsBase basicParamsBase) {
        composeRetry(composeEventHandling(observable, basicParamsBase), basicParamsBase)
            .doOnSubscribe(d -> basicParamsBase.startAction().run())
            .subscribe(orderEvent -> {},
                       basicParamsBase.errorConsumer()::accept,
                       basicParamsBase.completeAction()::run);
    }

    public Observable<OrderEvent> composeEventHandling(final Observable<OrderEvent> observable,
                                                       final BasicParamsBase basicParamsBase) {
        return observable
            .doOnNext(orderEvent -> handlerOrderEvent(orderEvent, basicParamsBase.consumerForEvent()));
    }

    private Observable<OrderEvent> composeRetry(final Observable<OrderEvent> observable,
                                                final CommonParamsBase commonParamsBase) {
        final int noOfRetries = commonParamsBase.noOfRetries();
        return noOfRetries > 0
                ? observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, commonParamsBase.delayInMillis()))
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

    public Observable<OrderEvent> composeTaskWithEventHandling(final Observable<OrderEvent> observable,
                                                               final BasicParamsBase basicParamsBase) {
        return composeTask(composeEventHandling(observable, basicParamsBase),
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
