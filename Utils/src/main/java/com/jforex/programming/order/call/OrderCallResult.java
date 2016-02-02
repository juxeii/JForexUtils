package com.jforex.programming.order.call;

import java.util.Optional;

public abstract class OrderCallResult {

    private final Optional<Exception> exceptionOpt;
    private final OrderCallRequest orderCallRequest;

    public OrderCallResult(final Optional<Exception> exceptionOpt,
                           final OrderCallRequest orderCallRequest) {
        this.exceptionOpt = exceptionOpt;
        this.orderCallRequest = orderCallRequest;
    }

    public final Optional<Exception> exceptionOpt() {
        return exceptionOpt;
    }

    public final OrderCallRequest callRequest() {
        return orderCallRequest;
    }
}
