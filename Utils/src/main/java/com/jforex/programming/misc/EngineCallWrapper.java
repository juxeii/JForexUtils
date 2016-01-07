package com.jforex.programming.misc;

import java.util.Collection;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCall;

public final class EngineCallWrapper {

    private final IEngine engine;

    public EngineCallWrapper(final IEngine engine) {
        this.engine = engine;
    }

    public final OrderCall submit(final OrderParams orderParams) {
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

    public final OrderCall merge(final String mergeOrderLabel,
                                 final Collection<IOrder> toMergeOrders) {
        return () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
    }
}
