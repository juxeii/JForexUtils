package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.jforex.programming.order.command.Command;
import com.jforex.programming.order.event.OrderEvent;

public interface CommonOption<T> {

    public T doOnOrderEvent(Consumer<OrderEvent> orderEventConsumer);

    public T retry(int noOfRetries,
                   long delayInMillis);

    public Command build();
}
