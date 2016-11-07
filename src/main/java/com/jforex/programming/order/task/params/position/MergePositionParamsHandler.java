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

    public MergePositionParamsHandler(final CancelSLTPTask cancelSLTPTask,
                              final BasicTask basicTask) {
        this.cancelSLTPTask = cancelSLTPTask;
        this.basicTask = basicTask;
    }

    public Observable<OrderEvent> observeCancelSLTP(final Collection<IOrder> toMergeOrders,
                                                    final ComplexMergePositionParams complexMergeParams) {
        return cancelSLTPTask.observe(toMergeOrders, complexMergeParams);
    }

    public Observable<OrderEvent> observeMerge(final Collection<IOrder> toMergeOrders,
                                               final MergePositionParams mergePositionParams) {
        final Instrument instrument = toMergeOrders.iterator().next().getInstrument();
        final Observable<OrderEvent> observable = basicTask.mergeOrders(mergePositionParams.mergeOrderLabel(),
                                                                        toMergeOrders);
        return TaskParamsUtil.composePositionTask(instrument,
                                                  observable,
                                                  mergePositionParams);
    }
}
