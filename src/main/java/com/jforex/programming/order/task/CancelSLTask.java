package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.MergeParams;

import io.reactivex.Observable;

public class CancelSLTask {

    private final BatchChangeTask batchChangeTask;

    public CancelSLTask(final BatchChangeTask orderChangeBatch) {
        this.batchChangeTask = orderChangeBatch;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> ordersForCancelSL,
                                          final MergeParams mergeParams) {
        return Observable.defer(() -> batchChangeTask.cancelSL(ordersForCancelSL,
                                                               mergeParams.orderCancelSLMode(),
                                                               mergeParams::orderCancelSLComposer));
    }
}
