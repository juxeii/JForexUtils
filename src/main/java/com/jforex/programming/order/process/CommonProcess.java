package com.jforex.programming.order.process;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import rx.Observable;

public abstract class CommonProcess {

    private final Consumer<Throwable> errorAction;
    private final int noOfRetries;
    private final long delayInMillis;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    protected CommonProcess(final CommonBuilder builder) {
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
        eventHandlerForType = builder.eventHandlerForType;
    }

    public final Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public final void start(final Observable<OrderEvent> observable) {
        evaluateRetry(observable)
            .subscribe(this::callEventHandler, errorAction::accept);
    }

    private final void callEventHandler(final OrderEvent orderEvent) {
        final OrderEventType type = orderEvent.type();
        if (eventHandlerForType.containsKey(type))
            eventHandlerForType
                .get(type)
                .accept(orderEvent.order());
    }

    private final Observable<OrderEvent> evaluateRetry(final Observable<OrderEvent> observable) {
        if (noOfRetries > 0) {
            final OrderProcessRetry orderProcessRetry = new OrderProcessRetry(noOfRetries, delayInMillis);
            return observable.retryWhen(orderProcessRetry::retryOnRejectObservable);
        }
        return observable;
    }
}
