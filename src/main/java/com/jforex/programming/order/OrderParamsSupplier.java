package com.jforex.programming.order;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.programming.builder.OrderParams;

@FunctionalInterface
public interface OrderParamsSupplier {

    public OrderParams forCommand(OrderCommand orderCommand);
}
