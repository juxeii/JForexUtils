package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class CancelSLTPTask {

    private final BatchCancelSLTask cancelSLTask;
    private final BatchCancelTPTask cancelTPTask;

    public CancelSLTPTask(final BatchCancelSLTask cancelSLTask,
                          final BatchCancelTPTask cancelTPTask) {
        this.cancelSLTask = cancelSLTask;
        this.cancelTPTask = cancelTPTask;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelSLTPOrders,
                                          final MergePositionParams mergePositionParams) {
        return Observable.defer(() -> toCancelSLTPOrders.size() < 2
                ? Observable.empty()
                : createTask(toCancelSLTPOrders, mergePositionParams));
    }

    private Observable<OrderEvent> createTask(final Collection<IOrder> toCancelSLTPOrders,
                                              final MergePositionParams mergePositionParams) {
        final Observable<OrderEvent> cancelSL = cancelSLTask.observe(toCancelSLTPOrders,
                                                                     mergePositionParams);
        final Observable<OrderEvent> cancelTP = cancelTPTask.observe(toCancelSLTPOrders,
                                                                     mergePositionParams);
        return arrangeObservables(cancelSL,
                                  cancelTP,
                                  mergePositionParams.mergeExecutionMode());
    }

    private Observable<OrderEvent> arrangeObservables(final Observable<OrderEvent> cancelSL,
                                                      final Observable<OrderEvent> cancelTP,
                                                      final CancelSLTPMode executionMode) {
        if (executionMode == CancelSLTPMode.ConcatCancelSLAndTP)
            return cancelSL.concatWith(cancelTP);
        else if (executionMode == CancelSLTPMode.ConcatCancelTPAndSL)
            return cancelTP.concatWith(cancelSL);
        return cancelSL.mergeWith(cancelTP);
    }
}
