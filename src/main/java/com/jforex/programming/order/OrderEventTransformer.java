package com.jforex.programming.order;

import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.ObservableTransformer;

public interface OrderEventTransformer extends ObservableTransformer<OrderEvent, OrderEvent> {
}
