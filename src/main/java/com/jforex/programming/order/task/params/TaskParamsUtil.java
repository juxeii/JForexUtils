package com.jforex.programming.order.task.params;

import java.util.Map;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

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
                                                       final SubscribeParams subscribeParams) {
        final int noOfRetries = subscribeParams.noOfRetries();
        return noOfRetries > 0
                ? observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, subscribeParams.delayInMillis()))
                : observable;
    }

    private static void handlerOrderEvent(final OrderEvent orderEvent,
                                          final Map<OrderEventType, OrderEventConsumer> consumerForEvent) {
        consumerForEvent.computeIfPresent(orderEvent.type(),
                                          (type, consumer) -> {
                                              consumer.accept(orderEvent);
                                              return consumer;
                                          });
    }
}
