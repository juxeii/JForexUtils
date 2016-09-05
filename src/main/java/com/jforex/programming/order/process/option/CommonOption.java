package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;

import rx.functions.Action0;

public interface CommonOption<T> {

    public T doOnStart(Action0 startAction);

    public T doOnCompleted(Action0 completedAction);

    public T doOnOrderEvent(Consumer<OrderEvent> orderEventAction);

    public T doOnError(Consumer<Throwable> errorAction);

    public T retry(int noOfRetries,
                   long delayInMillis);
}
