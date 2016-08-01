package com.jforex.programming.order.event;

import com.dukascopy.api.IOrder;

public final class OrderEvent {

    private final IOrder order;
    private final OrderEventType type;

    public OrderEvent(final IOrder order,
                      final OrderEventType type) {
        this.order = order;
        this.type = type;
    }

    public final IOrder order() {
        return order;
    }

    public final OrderEventType type() {
        return type;
    }
}
