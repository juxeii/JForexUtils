package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
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
        final Observable<OrderEvent> batchCancelTP = batchCancelTP(toCancelTPOrders, mergePositionParams);

        return taskParamsUtil.composeRetry(batchCancelTP, mergePositionParams.cancelTPRetryParams())
            .doOnSubscribe(d -> mergePositionParams.batchCancelTPStartAction().run())
            .doOnComplete(() -> mergePositionParams.batchCancelTPCompleteAction().run())
            .doOnError(mergePositionParams.batchCancelTPErrorConsumer()::accept);
    }

    private Observable<OrderEvent> batchCancelTP(final Collection<IOrder> toCancelTPOrders,
                                                 final MergePositionParams mergePositionParams) {
        return Observable.defer(() -> batchChangeTask.cancelTP(toCancelTPOrders, mergePositionParams));
    }
}
