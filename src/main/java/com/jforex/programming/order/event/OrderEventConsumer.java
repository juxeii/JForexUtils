package com.jforex.programming.order.event;

@FunctionalInterface
public interface OrderEventConsumer {

    public void onOrderEvent(OrderEvent orderEvent);
}
