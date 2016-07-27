package com.jforex.programming.order;

import com.dukascopy.api.IEngine.OrderCommand;

@FunctionalInterface
public interface OrderParamsSupplier {

    public OrderParams get(OrderCommand orderCommand);
}
