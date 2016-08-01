package com.jforex.programming.order.call;

import com.dukascopy.api.IOrder;

public class OrderCallRequest {

    private final IOrder order;
    private final OrderCallReason reason;

    public OrderCallRequest(final IOrder order,
                            final OrderCallReason reason) {
        this.order = order;
        this.reason = reason;
    }

    public final IOrder order() {
        return order;
    }

    public final OrderCallReason reason() {
        return reason;
    }
}
