package com.jforex.programming.order.event;

@FunctionalInterface
public interface OrderEventConsumer {

    abstract void onOrderEvent(OrderEvent orderEvent);
}
