package com.jforex.programming.order.call;

import java.util.Optional;

import com.dukascopy.api.IOrder;

public final class OrderCallExecutorResult {

    private final Optional<IOrder> maybeOrder;
    private final Optional<Exception> maybeException;

    public OrderCallExecutorResult(final Optional<IOrder> maybeOrder,
                                   final Optional<Exception> maybeException) {
        this.maybeOrder = maybeOrder;
        this.maybeException = maybeException;
    }

    public final Optional<IOrder> maybeOrder() {
        return maybeOrder;
    }

    public final Optional<Exception> maybeException() {
        return maybeException;
    }
}
