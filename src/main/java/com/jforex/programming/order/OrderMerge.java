package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class OrderMerge {

    private final OrderTaskExecutor orderTaskExecutor;
    private final OrderUtilHandler orderUtilHandler;

    public OrderMerge(final OrderTaskExecutor orderTaskExecutor,
                      final OrderUtilHandler orderUtilHandler) {
        this.orderTaskExecutor = orderTaskExecutor;
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> mergeOrders(final MergeCommand command) {
        final Collection<IOrder> toMergeOrders = command.toMergeOrders();
        final String mergeOrderLabel = command.mergeCommandWithParent().mergeOrderLabel();

        return mergeOrdersWithParams(mergeOrderLabel, toMergeOrders);
    }

    public Observable<OrderEvent> createMerge(final Collection<IOrder> toMergeOrders,
                                              final MergeCommandWithParent command) {
        return mergeOrdersWithParams(command.mergeOrderLabel(), toMergeOrders)
            .compose(command.mergeCompose());
    }

    private Observable<OrderEvent> mergeOrdersWithParams(final String mergeOrderLabel,
                                                         final Collection<IOrder> toMergeOrders) {
        return toMergeOrders.size() < 2
                ? Observable.empty()
                : orderTaskExecutor
                    .mergeOrders(mergeOrderLabel, toMergeOrders)
                    .toObservable()
                    .flatMap(order -> orderUtilHandler.callObservable(order, OrderCallReason.MERGE));
    }
}
