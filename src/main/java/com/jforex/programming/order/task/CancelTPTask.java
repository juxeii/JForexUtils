package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.BatchCancelSLTPParams;
import com.jforex.programming.order.task.params.position.CancelTPParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class CancelTPTask {

    private final BatchChangeTask batchChangeTask;
    private final TaskParamsUtil taskParamsUtil;

    public CancelTPTask(final BatchChangeTask batchChangeTask,
                        final TaskParamsUtil taskParamsUtil) {
        this.batchChangeTask = batchChangeTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelTPTPOrders,
                                          final MergePositionParams mergePositionParams) {
        final BatchCancelSLTPParams batchCancelSLTPParams = mergePositionParams.batchCancelSLTPParams();
        final Instrument instrument = toCancelTPTPOrders.iterator().next().getInstrument();

        return taskParamsUtil.composePositionTask(instrument,
                                                  batchCancelTP(toCancelTPTPOrders, batchCancelSLTPParams),
                                                  batchCancelSLTPParams.batchCancelTPParams());
    }

    private Observable<OrderEvent> batchCancelTP(final Collection<IOrder> toCancelTPTPOrders,
                                                 final BatchCancelSLTPParams batchCancelSLTPParams) {
        final CancelTPParams cancelTPParams = batchCancelSLTPParams
            .batchCancelTPParams()
            .cancelTPParams();
        final BatchMode batchMode = batchCancelSLTPParams
            .batchCancelTPParams()
            .batchMode();
        return Observable.defer(() -> batchChangeTask.cancelTP(toCancelTPTPOrders,
                                                               cancelTPParams,
                                                               batchMode));
    }
}
