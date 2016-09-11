package com.jforex.programming.order.event;

import com.dukascopy.api.IOrder;

public final class OrderEvent {

    private final IOrder order;
    private final OrderEventType type;
    private final boolean isInternal;

    public OrderEvent(final IOrder order,
                      final OrderEventType type,
                      final boolean isInternal) {
        this.order = order;
        this.type = type;
        this.isInternal = isInternal;
    }

    public final IOrder order() {
        return order;
    }

    public final OrderEventType type() {
        return type;
    }

    public boolean isInternal() {
        return isInternal;
    }

    @Override
    public String toString() {
        return "OrderEvent [" + order + ", type=" + type + " isInternal=" + isInternal + "]";
    }
}
