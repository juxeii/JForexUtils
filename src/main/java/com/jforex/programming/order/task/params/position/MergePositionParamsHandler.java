package com.jforex.programming.order.task.params.position;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.params.TaskParamsUtil;

import io.reactivex.Observable;

public class MergePositionParamsHandler {

    private final CancelSLTPTask cancelSLTPTask;
    private final BasicTask basicTask;
    private final TaskParamsUtil taskParamsUtil;

    public MergePositionParamsHandler(final CancelSLTPTask cancelSLTPTask,
                                      final BasicTask basicTask,
                                      final TaskParamsUtil taskParamsUtil) {
        this.cancelSLTPTask = cancelSLTPTask;
        this.basicTask = basicTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observeCancelSLTP(final Collection<IOrder> toMergeOrders,
                                                    final MergePositionParams complexMergeParams) {
        return cancelSLTPTask.observe(toMergeOrders, complexMergeParams);
    }

    public Observable<OrderEvent> observeMerge(final Collection<IOrder> toMergeOrders,
                                               final SimpleMergePositionParams mergePositionParams) {
        final Instrument instrument = toMergeOrders.iterator().next().getInstrument();
        final Observable<OrderEvent> observable = basicTask.mergeOrders(mergePositionParams.mergeOrderLabel(instrument),
                                                                        toMergeOrders);
        return taskParamsUtil.composePositionTask(instrument,
                                                  observable,
                                                  mergePositionParams);
    }
}
