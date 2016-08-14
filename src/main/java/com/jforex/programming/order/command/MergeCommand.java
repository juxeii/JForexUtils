package com.jforex.programming.order.command;

import static com.jforex.programming.order.event.OrderEventTypeData.mergeEventTypeData;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class MergeCommand implements OrderCallCommand {

    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.MERGE;
    private static final OrderEventTypeData orderEventTypeData = mergeEventTypeData;

    public MergeCommand(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders,
                        final IEngine engine) {
        callable = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    @Override
    public final OrderCallReason callReason() {
        return callReason;
    }
}
