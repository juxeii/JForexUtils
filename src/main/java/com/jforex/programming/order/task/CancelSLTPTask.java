package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.BatchCancelSLTPParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class CancelSLTPTask {

    private final BatchChangeTask batchChangeTask;
    private final TaskParamsUtil taskParamsUtil;

    public CancelSLTPTask(final BatchChangeTask batchChangeTask,
                          final TaskParamsUtil taskParamsUtil) {
        this.batchChangeTask = batchChangeTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelSLTPOrders,
                                          final MergePositionParams mergeParams) {
        return Observable.defer(() -> toCancelSLTPOrders.size() < 2
                ? Observable.empty()
                : createTask(toCancelSLTPOrders, mergeParams));
    }

    private Observable<OrderEvent> createTask(final Collection<IOrder> toCancelSLTPOrders,
                                              final MergePositionParams complexMergeParams) {
        final BatchCancelSLTPParams batchCancelSLAndTPParams = complexMergeParams.batchCancelSLTPParams();
        final Instrument instrument = toCancelSLTPOrders.iterator().next().getInstrument();

        final Observable<OrderEvent> cancelSL =
                taskParamsUtil.composePositionTask(instrument,
                                                   Observable.defer(() -> batchChangeTask.cancelSL(toCancelSLTPOrders,
                                                                                                   batchCancelSLAndTPParams
                                                                                                       .batchCancelSLParams()
                                                                                                       .cancelSLParams(),
                                                                                                   batchCancelSLAndTPParams
                                                                                                       .batchCancelSLParams()
                                                                                                       .batchMode())),
                                                   batchCancelSLAndTPParams.batchCancelSLParams());
        final Observable<OrderEvent> cancelTP =
                taskParamsUtil.composePositionTask(instrument,
                                                   Observable.defer(() -> batchChangeTask.cancelTP(toCancelSLTPOrders,
                                                                                                   batchCancelSLAndTPParams
                                                                                                       .batchCancelTPParams()
                                                                                                       .cancelTPParams(),
                                                                                                   batchCancelSLAndTPParams
                                                                                                       .batchCancelTPParams()
                                                                                                       .batchMode())),
                                                   batchCancelSLAndTPParams.batchCancelTPParams());

        final Observable<OrderEvent> observable = arrangeObservables(cancelSL,
                                                                     cancelTP,
                                                                     batchCancelSLAndTPParams
                                                                         .mergeExecutionMode());

        return taskParamsUtil.composePositionTask(instrument,
                                                  observable,
                                                  batchCancelSLAndTPParams);
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
