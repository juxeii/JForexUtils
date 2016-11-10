package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParamsHandler;

import io.reactivex.Observable;

public class CancelSLTPAndMergeTask {

    private final MergePositionParamsHandler mergePositionParamsHandler;

    public CancelSLTPAndMergeTask(final MergePositionParamsHandler mergePositionParamsHandler) {
        this.mergePositionParamsHandler = mergePositionParamsHandler;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toMergeOrders,
                                          final MergePositionParams mergePositionParams) {
        final Observable<OrderEvent> cancelSLTP = mergePositionParamsHandler
            .observeCancelSLTP(toMergeOrders, mergePositionParams);
        final Observable<OrderEvent> merge = mergePositionParamsHandler
            .observeMerge(toMergeOrders, mergePositionParams);

        return cancelSLTP.concatWith(merge);
    }
}
