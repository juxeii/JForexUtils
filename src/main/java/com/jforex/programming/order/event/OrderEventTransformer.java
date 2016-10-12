package com.jforex.programming.order.event;

import io.reactivex.ObservableTransformer;

public interface OrderEventTransformer extends
                                       ObservableTransformer<OrderEvent, OrderEvent> {
}
