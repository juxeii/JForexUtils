package com.jforex.programming.order;

import java.util.Optional;

import com.jforex.programming.order.call.OrderCallResult;

import com.dukascopy.api.IOrder;

public final class OrderChangeResult extends OrderCallResult {

    private final IOrder order;

    public OrderChangeResult(final IOrder order,
                             final Optional<Exception> exceptionOpt) {
        super(exceptionOpt);
        this.order = order;
    }

    public final IOrder order() {
        return order;
    }
}
