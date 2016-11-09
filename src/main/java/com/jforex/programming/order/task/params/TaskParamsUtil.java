package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.Instrument;
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

    public <T> Observable<OrderEvent> composePositionTask(final T item,
                                                          final Observable<OrderEvent> observable,
                                                          final PositionParamsBase<T> paramsForPosition) {
        return composeRetry(observable, paramsForPosition)
            .doOnSubscribe(d -> paramsForPosition.startAction(item).run())
            .doOnComplete(paramsForPosition.completeAction(item))
            .doOnError(paramsForPosition.errorConsumer(item)::accept);
    }

    public void subscribePositionTask(final Instrument instrument,
                                      Observable<OrderEvent> observable,
                                      final PositionParamsBase<Instrument> paramsForPosition) {
        observable = observable
            .doOnNext(orderEvent -> handlerOrderEvent(orderEvent, paramsForPosition.consumerForEvent()));
        composeRetry(observable, paramsForPosition)
            .doOnSubscribe(d -> paramsForPosition.startAction(instrument).run())
            .subscribe(orderEvent -> {},
                       e -> paramsForPosition.errorConsumer(instrument).accept(e),
                       paramsForPosition.completeAction(instrument)::run);
    }

    public void subscribeToAllPositionsTask(Observable<OrderEvent> observable,
                                            final BasicParamsBase basicParamsBase) {
        observable = observable
            .doOnNext(orderEvent -> handlerOrderEvent(orderEvent, basicParamsBase.consumerForEvent()));
        composeRetry(observable, basicParamsBase)
            .doOnSubscribe(d -> basicParamsBase.startAction().run())
            .subscribe(orderEvent -> {},
                       e -> basicParamsBase.errorConsumer().accept(e),
                       basicParamsBase.completeAction()::run);
    }
}
