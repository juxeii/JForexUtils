package com.jforex.programming.order.call;

import java.util.Optional;

import com.dukascopy.api.IOrder;

public final class OrderCallResult {

    private final Optional<IOrder> orderOpt;
    private final Optional<Exception> exceptionOpt;
    private final OrderCallRequest orderCallRequest;

    public OrderCallResult(final Optional<IOrder> orderOpt,
                           final Optional<Exception> exceptionOpt,
                           final OrderCallRequest orderCallRequest) {
        this.orderOpt = orderOpt;
        this.exceptionOpt = exceptionOpt;
        this.orderCallRequest = orderCallRequest;
    }

    public final Optional<IOrder> orderOpt() {
        return orderOpt;
    }

    public final Optional<Exception> exceptionOpt() {
        return exceptionOpt;
    }

    public final OrderCallRequest callRequest() {
        return orderCallRequest;
    }
}
