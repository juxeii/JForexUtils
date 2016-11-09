package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.BatchCancelSLTPParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class CancelSLTPTask {

    private final CancelSLTask cancelSLTask;
    private final CancelTPTask cancelTPTask;
    private final TaskParamsUtil taskParamsUtil;

    public CancelSLTPTask(final CancelSLTask cancelSLTask,
                          final CancelTPTask cancelTPTask,
                          final TaskParamsUtil taskParamsUtil) {
        this.cancelSLTask = cancelSLTask;
        this.cancelTPTask = cancelTPTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelSLTPOrders,
                                          final MergePositionParams mergePositionParams) {
        return Observable.defer(() -> toCancelSLTPOrders.size() < 2
                ? Observable.empty()
                : createTask(toCancelSLTPOrders, mergePositionParams));
    }

    private Observable<OrderEvent> createTask(final Collection<IOrder> toCancelSLTPOrders,
                                              final MergePositionParams mergePositionParams) {
        final BatchCancelSLTPParams batchCancelSLTPParams = mergePositionParams.batchCancelSLTPParams();

        final Observable<OrderEvent> cancelSL = cancelSLTask.observe(toCancelSLTPOrders,
                                                                     batchCancelSLTPParams.batchCancelSLParams());
        final Observable<OrderEvent> cancelTP = cancelTPTask.observe(toCancelSLTPOrders,
                                                                     batchCancelSLTPParams.batchCancelTPParams());

        final Observable<OrderEvent> batchObservables =
                arrangeObservables(cancelSL,
                                   cancelTP,
                                   batchCancelSLTPParams.mergeExecutionMode());

        return taskParamsUtil.composePositionTask(toCancelSLTPOrders.iterator().next().getInstrument(),
                                                  batchObservables,
                                                  batchCancelSLTPParams);
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
