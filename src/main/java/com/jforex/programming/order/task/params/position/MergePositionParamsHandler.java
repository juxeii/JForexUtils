package com.jforex.programming.order.task.params.position;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTaskObservable;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.params.TaskParamsUtil;

import io.reactivex.Observable;

public class MergePositionParamsHandler {

    private final CancelSLTPTask cancelSLTPTask;
    private final BasicTaskObservable basicTask;
    private final TaskParamsUtil taskParamsUtil;

    public MergePositionParamsHandler(final CancelSLTPTask cancelSLTPTask,
                                      final BasicTaskObservable basicTask,
                                      final TaskParamsUtil taskParamsUtil) {
        this.cancelSLTPTask = cancelSLTPTask;
        this.basicTask = basicTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observeCancelSLTP(final Collection<IOrder> toMergeOrders,
                                                    final MergePositionParams mergePositionParams) {
        return cancelSLTPTask.observe(toMergeOrders, mergePositionParams);
    }

    public Observable<OrderEvent> observeMerge(final Collection<IOrder> toMergeOrders,
                                               final SimpleMergePositionParams simpleMergePositionParams) {
        final Observable<OrderEvent> observable =
                basicTask.mergeOrders(simpleMergePositionParams.mergeOrderLabel(), toMergeOrders);
        return taskParamsUtil.composePositionTask(observable, simpleMergePositionParams);
    }
}
