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
        final Observable<OrderEvent> observable =
                cancelSLTPTask.observe(toMergeOrders, mergePositionParams);

        return taskParamsUtil
            .composeRetry(observable, mergePositionParams.cancelSLTPRetryParams())
            .doOnSubscribe(d -> mergePositionParams.cancelSLTPStartAction().run())
            .doOnComplete(() -> mergePositionParams.cancelSLTPCompleteAction().run())
            .doOnError(mergePositionParams.cancelSLTPErrorConsumer()::accept);
    }

    public Observable<OrderEvent> observeMerge(final Collection<IOrder> toMergeOrders,
                                               final MergePositionParams mergePositionParams) {
        final Observable<OrderEvent> observable =
                basicTask.mergeOrders(mergePositionParams.mergeOrderLabel(), toMergeOrders);

        return taskParamsUtil
            .composeRetry(observable, mergePositionParams.mergeRetryParams())
            .doOnSubscribe(d -> mergePositionParams.mergeStartAction().run())
            .doOnComplete(() -> mergePositionParams.mergeCompleteAction().run())
            .doOnError(mergePositionParams.mergeErrorConsumer()::accept);
    }
}
