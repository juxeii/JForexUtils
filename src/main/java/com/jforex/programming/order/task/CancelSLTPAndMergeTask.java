package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.position.ComplexMergePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParamsHandler;

import io.reactivex.Observable;

public class CancelSLTPAndMergeTask {

    private final MergePositionParamsHandler mergeParamsHandler;

    public CancelSLTPAndMergeTask(final MergePositionParamsHandler mergeParamsHandler) {
        this.mergeParamsHandler = mergeParamsHandler;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toMergeOrders,
                                          final ComplexMergePositionParams complexMergeParams) {
        final Observable<OrderEvent> cancelSLTP = mergeParamsHandler.observeCancelSLTP(toMergeOrders,
                                                                                       complexMergeParams);
        final Observable<OrderEvent> merge = mergeParamsHandler.observeMerge(toMergeOrders,
                                                                             complexMergeParams.mergePositionParams());

        return cancelSLTP.concatWith(merge);
    }
}
