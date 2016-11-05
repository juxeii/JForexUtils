package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.ComplexMergeParams;
import com.jforex.programming.order.task.params.MergeParamsHandler;

import io.reactivex.Observable;

public class CancelSLTPAndMergeTask {

    private final MergeParamsHandler mergeParamsHandler;

    public CancelSLTPAndMergeTask(final MergeParamsHandler mergeParamsHandler) {
        this.mergeParamsHandler = mergeParamsHandler;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toMergeOrders,
                                          final ComplexMergeParams mergeParams) {
        final Observable<OrderEvent> cancelSLTP = mergeParamsHandler.observeCancelSLTP(toMergeOrders, mergeParams);
        final Observable<OrderEvent> merge = mergeParamsHandler.observeMerge(toMergeOrders, mergeParams);

        return cancelSLTP.concatWith(merge);
    }
}
