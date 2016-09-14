package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.jforex.programming.order.command.Command;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.functions.Action;

public interface CommonOption<T> {

    public T doOnStart(Action startAction);

    public T doOnComplete(Action completedAction);

    public T doOnOrderEvent(Consumer<OrderEvent> orderEventConsumer);

    public T doOnError(Consumer<Throwable> errorConsumer);

    public T retry(int noOfRetries,
                   long delayInMillis);

    public Command build();
}
