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
                                              final MergePositionParams mergePositionParams) {
        final BatchCancelSLTPParams batchCancelSLAndTPParams = mergePositionParams.batchCancelSLTPParams();
        final Instrument instrument = toCancelSLTPOrders.iterator().next().getInstrument();

        final Observable<OrderEvent> cancelSL = composeBatchCancelSL(instrument,
                                                                     toCancelSLTPOrders,
                                                                     batchCancelSLAndTPParams);
        final Observable<OrderEvent> cancelTP = composeBatchCancelTP(instrument,
                                                                     toCancelSLTPOrders,
                                                                     batchCancelSLAndTPParams);
        final Observable<OrderEvent> batchObservables =
                arrangeObservables(cancelSL,
                                   cancelTP,
                                   batchCancelSLAndTPParams.mergeExecutionMode());

        return taskParamsUtil.composePositionTask(instrument,
                                                  batchObservables,
                                                  batchCancelSLAndTPParams);
    }

    private Observable<OrderEvent> composeBatchCancelSL(final Instrument instrument,
                                                        final Collection<IOrder> toCancelSLTPOrders,
                                                        final BatchCancelSLTPParams batchCancelSLAndTPParams) {
        return taskParamsUtil.composePositionTask(instrument,
                                                  batchCancelSL(toCancelSLTPOrders, batchCancelSLAndTPParams),
                                                  batchCancelSLAndTPParams.batchCancelSLParams());
    }

    private Observable<OrderEvent> composeBatchCancelTP(final Instrument instrument,
                                                        final Collection<IOrder> toCancelSLTPOrders,
                                                        final BatchCancelSLTPParams batchCancelSLAndTPParams) {
        return taskParamsUtil.composePositionTask(instrument,
                                                  batchCancelTP(toCancelSLTPOrders, batchCancelSLAndTPParams),
                                                  batchCancelSLAndTPParams.batchCancelTPParams());
    }

    private Observable<OrderEvent> batchCancelSL(final Collection<IOrder> toCancelSLTPOrders,
                                                 final BatchCancelSLTPParams batchCancelSLAndTPParams) {
        return Observable.defer(() -> batchChangeTask.cancelSL(toCancelSLTPOrders,
                                                               batchCancelSLAndTPParams
                                                                   .batchCancelSLParams()
                                                                   .cancelSLParams(),
                                                               batchCancelSLAndTPParams
                                                                   .batchCancelSLParams()
                                                                   .batchMode()));
    }

    private Observable<OrderEvent> batchCancelTP(final Collection<IOrder> toCancelSLTPOrders,
                                                 final BatchCancelSLTPParams batchCancelSLAndTPParams) {
        return Observable.defer(() -> batchChangeTask.cancelTP(toCancelSLTPOrders,
                                                               batchCancelSLAndTPParams
                                                                   .batchCancelTPParams()
                                                                   .cancelTPParams(),
                                                               batchCancelSLAndTPParams
                                                                   .batchCancelTPParams()
                                                                   .batchMode()));
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
