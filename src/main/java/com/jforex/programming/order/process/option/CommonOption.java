package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;

import rx.functions.Action0;

public interface CommonOption<T> {

    public T onCompleted(Action0 completedAction);

    public T onEvent(Consumer<OrderEvent> eventAction);

    public T onError(Consumer<Throwable> errorAction);

    public T doRetries(int noOfRetries,
                       long delayInMillis);
}
