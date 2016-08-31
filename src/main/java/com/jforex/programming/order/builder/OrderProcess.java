package com.jforex.programming.order.builder;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import rx.Observable;

public abstract class OrderProcess {

    protected Consumer<Throwable> errorAction;
    protected int noOfRetries;
    protected long delayInMillis;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    protected OrderProcess(final CommonProcess<?> builder) {
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
        eventHandlerForType = builder.eventHandlerForType;
    }

    public final Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public void start(final Observable<OrderEvent> observable) {
        evaluateRetry(observable)
            .subscribe(this::callEventHandler, errorAction::accept);
    }

    protected void callEventHandler(final OrderEvent orderEvent) {
        final OrderEventType type = orderEvent.type();
        if (eventHandlerForType.containsKey(type))
            eventHandlerForType
                .get(type)
                .accept(orderEvent.order());
    }

    private final Observable<OrderEvent> evaluateRetry(final Observable<OrderEvent> observable) {
        if (noOfRetries > 0) {
            final OrderProcessRetry orderCallRetry = new OrderProcessRetry(noOfRetries, delayInMillis);
            return observable.retryWhen(orderCallRetry::retryOnRejectObservable);
        }
        return observable;
    }
}
