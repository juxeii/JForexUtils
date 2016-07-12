package com.jforex.programming.order.call;

import com.jforex.programming.order.event.OrderEvent;

public final class OrderCallRejectException extends Exception {

    private final OrderEvent orderEvent;

    private static final long serialVersionUID = 1L;

    public OrderCallRejectException(final String message,
                                    final OrderEvent orderEvent) {
        super(message);
        this.orderEvent = orderEvent;
    }

    public final OrderEvent orderEvent() {
        return orderEvent;
    }
}
