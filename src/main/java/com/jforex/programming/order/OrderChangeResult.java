package com.jforex.programming.order;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderCallResult;

public final class OrderChangeResult extends OrderCallResult {

    private final IOrder order;

    public OrderChangeResult(final IOrder order,
                             final Optional<Exception> exceptionOpt,
                             final OrderCallRequest orderCallRequest) {
        super(exceptionOpt, orderCallRequest);
        this.order = order;
    }

    public final IOrder order() {
        return order;
    }
}
