package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.MergeParams;

import io.reactivex.Observable;

public class CancelSLTPTask {

    private final CancelSLTask cancelSLTask;
    private final CancelTPTask cancelTPTask;

    public CancelSLTPTask(final CancelSLTask orderCancelSL,
                          final CancelTPTask orderCancelTP) {
        this.cancelSLTask = orderCancelSL;
        this.cancelTPTask = orderCancelTP;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelSLTPOrders,
                                          final MergeParams mergeParams) {
        return Observable.defer(() -> toCancelSLTPOrders.size() < 2
                ? Observable.empty()
                : createTask(toCancelSLTPOrders, mergeParams));
    }

    private Observable<OrderEvent> createTask(final Collection<IOrder> toCancelSLTPOrders,
                                              final MergeParams mergeParams) {
        final Observable<OrderEvent> cancelSL = cancelSLTask.observe(toCancelSLTPOrders, mergeParams);
        final Observable<OrderEvent> cancelTP = cancelTPTask.observe(toCancelSLTPOrders, mergeParams);

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
