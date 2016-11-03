package com.jforex.programming.order.spec;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;

public interface OrderEventConsumer extends Consumer<OrderEvent> {
}
