package com.jforex.programming.order.event;

import com.dukascopy.api.IOrder;

public final class OrderEvent {

    private final IOrder order;
    private final OrderEventType orderEventType;

    public OrderEvent(final IOrder order,
                      final OrderEventType orderEventType) {
        this.order = order;
        this.orderEventType = orderEventType;
    }

    public final IOrder order() {
        return order;
    }

    public final OrderEventType type() {
        return orderEventType;
    }
}
