package com.jforex.programming.order.task.params;

import java.util.Map;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;

public class BasicTaskParams {

    private final ComposeParams composeParams;
    private final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    private final int noOfRetries;
    private final long delayInMillis;

    protected BasicTaskParams(final ParamsBuilderBase<?> builder) {
        composeParams = new ComposeParams(builder);
        consumerForEvent = composeParams.consumerForEvent();
        noOfRetries = composeParams.noOfRetries();
        delayInMillis = composeParams.delayInMillis();
    }

    protected void subscribe(final Observable<OrderEvent> observable) {
        composeRetry(observable)
            .doOnSubscribe(d -> composeParams.startAction().run())
            .doOnComplete(composeParams.completeAction())
            .doOnError(t -> composeParams.errorConsumer().accept(t))
            .doOnNext(this::handlerOrderEvent);
    }

    private Observable<OrderEvent> composeRetry(final Observable<OrderEvent> observable) {
        return noOfRetries > 0
                ? observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, delayInMillis))
                : observable;
    }

    private void handlerOrderEvent(final OrderEvent orderEvent) {
        consumerForEvent.computeIfPresent(orderEvent.type(),
                                          (type, consumer) -> {
                                              consumer.accept(orderEvent);
                                              return consumer;
                                          });
    }
}
