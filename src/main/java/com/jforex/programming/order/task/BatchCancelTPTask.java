package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class BatchCancelTPTask {

    private final BatchChangeTask batchChangeTask;
    private final TaskParamsUtil taskParamsUtil;

    public BatchCancelTPTask(final BatchChangeTask batchChangeTask,
                             final TaskParamsUtil taskParamsUtil) {
        this.batchChangeTask = batchChangeTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelTPOrders,
                                          final MergePositionParams mergePositionParams) {
        return Observable.defer(() -> {
            final Observable<OrderEvent> batchCancelTP =
                    batchChangeTask.cancelTP(toCancelTPOrders, mergePositionParams);
            final TaskParamsBase batchCancelTPParams = mergePositionParams.batchCancelTPParams();
            return taskParamsUtil.compose(batchCancelTP, batchCancelTPParams);
        });
    }
}
