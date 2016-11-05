package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.ComplexMergeParams;

import io.reactivex.Observable;

public class CancelSLTPTask {

    private final BatchChangeTask batchChangeTask;

    public CancelSLTPTask(final BatchChangeTask batchChangeTask) {
        this.batchChangeTask = batchChangeTask;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelSLTPOrders,
                                          final ComplexMergeParams mergeParams) {
        return Observable.defer(() -> toCancelSLTPOrders.size() < 2
                ? Observable.empty()
                : createTask(toCancelSLTPOrders, mergeParams));
    }

    private Observable<OrderEvent> createTask(final Collection<IOrder> toCancelSLTPOrders,
                                              final ComplexMergeParams mergeParams) {
        final Observable<OrderEvent> cancelSL =
                Observable.defer(() -> batchChangeTask.cancelSL(toCancelSLTPOrders,
                                                                mergeParams.orderCancelSLMode(),
                                                                mergeParams::orderCancelSLComposer));
        final Observable<OrderEvent> cancelTP =
                Observable.defer(() -> batchChangeTask.cancelTP(toCancelSLTPOrders,
                                                                mergeParams.orderCancelTPMode(),
                                                                mergeParams::orderCancelTPComposer));

        return arrangeObservables(cancelSL, cancelTP, mergeParams.executionMode())
            .compose(mergeParams.cancelSLTPComposer());
    }

    private Observable<OrderEvent> arrangeObservables(final Observable<OrderEvent> cancelSL,
                                                      final Observable<OrderEvent> cancelTP,
                                                      final MergeExecutionMode executionMode) {
        if (executionMode == MergeExecutionMode.ConcatCancelSLAndTP)
            return cancelSL.concatWith(cancelTP);
        else if (executionMode == MergeExecutionMode.ConcatCancelTPAndSL)
            return cancelTP.concatWith(cancelSL);
        return cancelSL.mergeWith(cancelTP);
    }
}
