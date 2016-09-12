package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.functions.Action;

public interface CommonOption<T> {

    public T doOnStart(Action startAction);

    public T doOnCompleted(Action completedAction);

    public T doOnOrderEvent(Consumer<OrderEvent> orderEventAction);

    public T doOnError(Consumer<Throwable> errorAction);

    public T retry(int noOfRetries,
                   long delayInMillis);
}
