package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;
import com.jforex.programming.order.task.params.basic.BasicParamsBase;
import com.jforex.programming.order.task.params.position.PositionParamsBase;

import io.reactivex.Observable;

public class TaskParamsUtil {

    public void subscribeBasicParams(Observable<OrderEvent> observable,
                                     final BasicParamsBase basicParamsBase) {
        observable = observable
            .doOnNext(orderEvent -> handlerOrderEvent(orderEvent, basicParamsBase.consumerForEvent()));
        composeRetry(observable, basicParamsBase)
            .doOnSubscribe(d -> basicParamsBase.startAction().run())
            .subscribe(orderEvent -> {},
                       e -> basicParamsBase.errorConsumer().accept(e),
                       basicParamsBase.completeAction());
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
        return composeTask(observable.doOnNext(orderEvent -> handlerOrderEvent(orderEvent,
                                                                               basicParamsBase
                                                                                   .consumerForEvent())),
                           basicParamsBase);
    }

    public void subscribePositionTask(final Observable<OrderEvent> observable,
                                      final PositionParamsBase positionParamsBase) {
        composeRetry(observable, positionParamsBase)
            .doOnSubscribe(d -> positionParamsBase.startAction().run())
            .subscribe(orderEvent -> {},
                       positionParamsBase.errorConsumer()::accept,
                       positionParamsBase.completeAction()::run);
    }

    public void subscribeToAllPositionsTask(final Observable<OrderEvent> observable,
                                            final BasicParamsBase basicParamsBase) {
        composeRetry(observable, basicParamsBase)
            .doOnSubscribe(d -> basicParamsBase.startAction().run())
            .subscribe(orderEvent -> {},
                       basicParamsBase.errorConsumer()::accept,
                       basicParamsBase.completeAction()::run);
    }
}
