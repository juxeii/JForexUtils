package com.jforex.programming.order.call;

import java.util.Optional;

import com.dukascopy.api.IOrder;

public final class OrderExecutorResult {

    private final Optional<IOrder> orderOpt;
    private final Optional<Exception> exceptionOpt;

    public OrderExecutorResult(final Optional<IOrder> orderOpt,
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
