package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class BatchCancelSLTask {

    private final BatchChangeTask batchChangeTask;
    private final TaskParamsUtil taskParamsUtil;

    public BatchCancelSLTask(final BatchChangeTask batchChangeTask,
                             final TaskParamsUtil taskParamsUtil) {
        this.batchChangeTask = batchChangeTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelSLOrders,
                                          final MergePositionParams mergePositionParams) {
        final Observable<OrderEvent> batchCancelSL = batchCancelSL(toCancelSLOrders, mergePositionParams);
        return taskParamsUtil.composeParams(batchCancelSL,
                                            mergePositionParams.batchCancelSLComposeParams());
    }

    private Observable<OrderEvent> batchCancelSL(final Collection<IOrder> toCancelSLOrders,
                                                 final MergePositionParams mergePositionParams) {
        return Observable.defer(() -> batchChangeTask.cancelSL(toCancelSLOrders, mergePositionParams));
    }
}
