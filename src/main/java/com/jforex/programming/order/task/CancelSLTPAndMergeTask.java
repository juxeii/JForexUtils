package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.MergeParamsForPosition;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class CancelSLTPAndMergeTask {

    private final CancelSLTPTask cancelSLTPTask;
    private final BasicTask basicTask;
    private final TaskParamsUtil taskParamsUtil;

    public CancelSLTPAndMergeTask(final CancelSLTPTask cancelSLTPTask,
                                  final BasicTask basicTask,
                                  final TaskParamsUtil taskParamsUtil) {
        this.cancelSLTPTask = cancelSLTPTask;
        this.basicTask = basicTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toMergeOrders,
                                          final MergePositionParams mergePositionParams) {
        final TaskParamsBase cancelSLTPParams = mergePositionParams.cancelSLTPParams();
        final Observable<OrderEvent> cancelSLTP =
                taskParamsUtil.compose(cancelSLTPTask.observe(toMergeOrders, mergePositionParams),
                                       cancelSLTPParams);

        final MergeParamsForPosition mergeParamsForPosition = mergePositionParams.mergeParamsForPosition();
        final String mergeOrderLabel = mergePositionParams.mergeOrderLabel();
        final Observable<OrderEvent> merge =
                taskParamsUtil.compose(basicTask.mergeOrders(mergeOrderLabel, toMergeOrders),
                                       mergeParamsForPosition);

        return cancelSLTP.concatWith(merge);
    }
}
