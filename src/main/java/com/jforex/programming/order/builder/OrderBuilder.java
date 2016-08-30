package com.jforex.programming.order.builder;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public abstract class OrderBuilder {

    protected Consumer<Throwable> errorAction;
    protected int noOfRetries;
    protected long delayInMillis;

    protected OrderBuilder(final CommonBuilder<?> builder) {
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
    }

    public final Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public final int noOfRetries() {
        return noOfRetries;
    }

    public final long delayInMillis() {
        return delayInMillis;
    }

    public void startObservable(final Observable<OrderEvent> observable) {
        evaluateRetry(observable)
            .subscribe(this::callEventHandler, errorAction::accept);
    }

    protected abstract void callEventHandler(OrderEvent orderEvent);

    private final Observable<OrderEvent> evaluateRetry(final Observable<OrderEvent> observable) {
        if (noOfRetries() > 0) {
            final OrderCallRetry orderCallRetry = new OrderCallRetry(noOfRetries, delayInMillis);
            return observable.retryWhen(orderCallRetry::retryOnRejectObservable);
        }
        return observable;
    }
}
