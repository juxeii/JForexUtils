package com.jforex.programming.order.call;

import java.util.Optional;

public abstract class OrderCallResult {

    private final Optional<Exception> exceptionOpt;

    public OrderCallResult(final Optional<Exception> exceptionOpt) {
        this.exceptionOpt = exceptionOpt;
    }

    public final Optional<Exception> exceptionOpt() {
        return exceptionOpt;
    }
}
