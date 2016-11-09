package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.BatchCancelSLTPParams;
import com.jforex.programming.order.task.params.position.CancelSLParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class CancelSLTask {

    private final BatchChangeTask batchChangeTask;
    private final TaskParamsUtil taskParamsUtil;

    public CancelSLTask(final BatchChangeTask batchChangeTask,
                        final TaskParamsUtil taskParamsUtil) {
        this.batchChangeTask = batchChangeTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelSLTPOrders,
                                          final MergePositionParams mergePositionParams) {
        final BatchCancelSLTPParams batchCancelSLTPParams = mergePositionParams.batchCancelSLTPParams();
        final Instrument instrument = toCancelSLTPOrders.iterator().next().getInstrument();

        return taskParamsUtil.composePositionTask(instrument,
                                                  batchCancelSL(toCancelSLTPOrders, batchCancelSLTPParams),
                                                  batchCancelSLTPParams.batchCancelSLParams());
    }

    private Observable<OrderEvent> batchCancelSL(final Collection<IOrder> toCancelSLTPOrders,
                                                 final BatchCancelSLTPParams batchCancelSLTPParams) {
        final CancelSLParams cancelSLParams = batchCancelSLTPParams
            .batchCancelSLParams()
            .cancelSLParams();
        final BatchMode batchMode = batchCancelSLTPParams
            .batchCancelSLParams()
            .batchMode();
        return Observable.defer(() -> batchChangeTask.cancelSL(toCancelSLTPOrders,
                                                               cancelSLParams,
                                                               batchMode));
    }
}
