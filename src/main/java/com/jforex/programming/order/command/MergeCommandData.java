package com.jforex.programming.order.command;

import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.Callable;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class MergeCommandData implements CommandData {

    private final Callable<IOrder> callable;

    private static final OrderEventTypeData orderEventTypeData =
            new OrderEventTypeData(EnumSet.of(MERGE_OK, MERGE_CLOSE_OK),
                                   EnumSet.of(MERGE_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    public MergeCommandData(final String mergeOrderLabel,
                            final Collection<IOrder> toMergeOrders,
                            final IEngine engine) {
        callable = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final OrderCallReason callReason() {
        return OrderCallReason.MERGE;
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }
}
