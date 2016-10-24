package com.jforex.programming.order.task.params;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CancelSLTPTask;

import io.reactivex.Observable;

public class MergeParamsHandler {

    private final CancelSLTPTask orderCancelSLAndTP;
    private final BasicTask orderBasicTask;

    public MergeParamsHandler(final CancelSLTPTask orderCancelSLAndTP,
                              final BasicTask orderBasicTask) {
        this.orderCancelSLAndTP = orderCancelSLAndTP;
        this.orderBasicTask = orderBasicTask;
    }

    public Observable<OrderEvent> observeCancelSLTP(final Collection<IOrder> toMergeOrders,
                                                    final MergeParams mergeParams) {
        return orderCancelSLAndTP.observe(toMergeOrders, mergeParams);
    }

    public Observable<OrderEvent> observeMerge(final Collection<IOrder> toMergeOrders,
                                               final MergeParams mergeParams) {
        return orderBasicTask
            .mergeOrders(mergeParams.mergeOrderLabel(), toMergeOrders)
            .compose(mergeParams.mergeComposer());
    }
}
