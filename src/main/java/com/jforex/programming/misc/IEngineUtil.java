package com.jforex.programming.misc;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;

public class IEngineUtil {

    private final IEngine engine;

    public IEngineUtil(final IEngine engine) {
        this.engine = engine;
    }

    public Callable<IOrder> submitCallable(final OrderParams orderParams) {
        return () -> engine.submitOrder(orderParams.label(),
                                        orderParams.instrument(),
                                        orderParams.orderCommand(),
                                        orderParams.amount(),
                                        orderParams.price(),
                                        orderParams.slippage(),
                                        orderParams.stopLossPrice(),
                                        orderParams.takeProfitPrice(),
                                        orderParams.goodTillTime(),
                                        orderParams.comment());
    }

    public Callable<IOrder> mergeCallable(final String mergeOrderLabel,
                                          final Collection<IOrder> toMergeOrders) {
        return () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
    }
}
