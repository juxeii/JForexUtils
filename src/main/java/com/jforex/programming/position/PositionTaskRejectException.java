package com.jforex.programming.position;

import com.jforex.programming.order.event.OrderEvent;

public final class PositionTaskRejectException extends Exception {

    private final OrderEvent orderEvent;

    private final static long serialVersionUID = 1L;

    public PositionTaskRejectException(final String message,
                                       final OrderEvent orderEvent) {
        super(message);
        this.orderEvent = orderEvent;
    }

    public final OrderEvent orderEvent() {
        return orderEvent;
    }
}
