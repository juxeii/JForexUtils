package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;
import com.jforex.programming.order.task.params.basic.SubscribeParams;
import com.jforex.programming.order.task.params.position.PositionParamsBase;

import io.reactivex.Observable;

public class TaskParamsUtil {

    public static void subscribe(final Observable<OrderEvent> observable,
                                 final SubscribeParams subscribeParams) {
        composeRetry(observable, subscribeParams)
            .doOnSubscribe(d -> subscribeParams.startAction().run())
            .subscribe(orderEvent -> handlerOrderEvent(orderEvent, subscribeParams.consumerForEvent()),
                       e -> subscribeParams.errorConsumer().accept(e),
                       subscribeParams.completeAction());
    }

    private static Observable<OrderEvent> composeRetry(final Observable<OrderEvent> observable,
                                                       final RetryParams retryParams) {
        final int noOfRetries = retryParams.noOfRetries();
        return noOfRetries > 0
                ? observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, retryParams.delayInMillis()))
                : observable;
    }

    private static void handlerOrderEvent(final OrderEvent orderEvent,
                                          final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent) {
        consumerForEvent.computeIfPresent(orderEvent.type(),
                                          (type, consumer) -> {
                                              consumer.accept(orderEvent);
                                              return consumer;
                                          });
    }

    public static <T> Observable<OrderEvent> composePositionTask(final T item,
                                                                 final Observable<OrderEvent> observable,
                                                                 final PositionParamsBase<T> paramsForPosition) {
        return composeRetry(observable, paramsForPosition)
            .doOnSubscribe(d -> paramsForPosition.startAction(item).run())
            .doOnComplete(paramsForPosition.completeAction(item))
            .doOnError(paramsForPosition.errorConsumer(item)::accept);
    }

    public static void subscribePositionTask(final Instrument instrument,
                                             final Observable<OrderEvent> observable,
                                             final PositionParamsBase<Instrument> paramsForPosition) {
        composeRetry(observable, paramsForPosition)
            .doOnSubscribe(d -> paramsForPosition.startAction(instrument).run())
            .subscribe(orderEvent -> handlerOrderEvent(orderEvent, paramsForPosition.consumerForEvent()),
                       e -> paramsForPosition.errorConsumer(instrument).accept(e),
                       paramsForPosition.completeAction(instrument)::run);
    }
}
