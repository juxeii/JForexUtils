package com.jforex.programming.order;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderCallResult;

public final class OrderCreateResult extends OrderCallResult {

    private final Optional<IOrder> orderOpt;

    public OrderCreateResult(final Optional<IOrder> orderOpt,
                             final Optional<Exception> exceptionOpt,
                             final OrderCallRequest orderCallRequest) {
        super(exceptionOpt, orderCallRequest);
        this.orderOpt = orderOpt;
    }

    public final Optional<IOrder> orderOpt() {
        return orderOpt;
    }
}
