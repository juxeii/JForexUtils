package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.BatchCancelTPParams;

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
                                          final BatchCancelTPParams batchCancelTPParams) {
        return taskParamsUtil.composeTask(batchCancelTP(toCancelTPTPOrders, batchCancelTPParams),
                                          batchCancelTPParams);
    }

    private Observable<OrderEvent> batchCancelTP(final Collection<IOrder> toCancelTPTPOrders,
                                                 final BatchCancelTPParams batchCancelTPParams) {
        return Observable.defer(() -> batchChangeTask.cancelTP(toCancelTPTPOrders,
                                                               batchCancelTPParams.cancelTPParamsFactory(),
                                                               batchCancelTPParams.batchMode()));
    }
}
