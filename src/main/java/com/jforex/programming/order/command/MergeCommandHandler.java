package com.jforex.programming.order.command;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.OrderBasicTask;
import com.jforex.programming.order.task.OrderCancelSLAndTP;

import io.reactivex.Observable;

public class MergeCommandHandler {

    private final OrderCancelSLAndTP orderCancelSLAndTP;
    private final OrderBasicTask orderBasicTask;

    public MergeCommandHandler(final OrderCancelSLAndTP orderCancelSLAndTP,
                               final OrderBasicTask orderBasicTask) {
        this.orderCancelSLAndTP = orderCancelSLAndTP;
        this.orderBasicTask = orderBasicTask;
    }

    public Observable<OrderEvent> observeCancelSLTP(final Collection<IOrder> toMergeOrders,
                                                    final MergeCommand command) {
        return orderCancelSLAndTP.observe(toMergeOrders, command);
    }

    public Observable<OrderEvent> observeMerge(final Collection<IOrder> toMergeOrders,
                                               final MergeCommand command) {
        return orderBasicTask
            .mergeOrders(command.mergeOrderLabel(), toMergeOrders)
            .compose(command.mergeComposer());
    }
}
