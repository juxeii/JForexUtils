package com.jforex.programming.order.task.params;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CancelSLTPTask;

import io.reactivex.Observable;

public class MergeParamsHandler {

    private final CancelSLTPTask cancelSLTPTask;
    private final BasicTask basicTask;

    public MergeParamsHandler(final CancelSLTPTask cancelSLTPTask,
                              final BasicTask basicTask) {
        this.cancelSLTPTask = cancelSLTPTask;
        this.basicTask = basicTask;
    }

    public Observable<OrderEvent> observeCancelSLTP(final Collection<IOrder> toMergeOrders,
                                                    final ComplexMergeParams mergeParams) {
        return cancelSLTPTask.observe(toMergeOrders, mergeParams);
    }

    public Observable<OrderEvent> observeMerge(final Collection<IOrder> toMergeOrders,
                                               final ComplexMergeParams mergeParams) {
        return basicTask
            .mergeOrders(mergeParams.mergeOrderLabel(), toMergeOrders)
            .compose(mergeParams.mergeComposer());
    }
}
