package com.jforex.programming.order;

import java.util.Optional;

import com.dukascopy.api.IOrder;

public final class OrderCreateResult {

    private final Optional<IOrder> orderOpt;
    private final Optional<Exception> exceptionOpt;

    public OrderCreateResult(final Optional<IOrder> orderOpt,
                             final Optional<Exception> exceptionOpt) {
        this.orderOpt = orderOpt;
        this.exceptionOpt = exceptionOpt;
    }

    public final Optional<IOrder> orderOpt() {
        return orderOpt;
    }

    public final Optional<Exception> exceptionOpt() {
        return exceptionOpt;
    }
}
