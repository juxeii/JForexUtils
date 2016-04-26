package com.jforex.programming.order;

import java.util.Optional;

import com.jforex.programming.order.call.OrderCallResult;

import com.dukascopy.api.IOrder;

public final class OrderCreateResult extends OrderCallResult {

    private final Optional<IOrder> orderOpt;

    public OrderCreateResult(final Optional<IOrder> orderOpt,
                             final Optional<Exception> exceptionOpt) {
        super(exceptionOpt);
        this.orderOpt = orderOpt;
    }

    public final Optional<IOrder> orderOpt() {
        return orderOpt;
    }
}
