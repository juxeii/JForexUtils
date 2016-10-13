package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class CancelSLTask {

    private final BatchChangeTask batchChangeTask;

    public CancelSLTask(final BatchChangeTask orderChangeBatch) {
        this.batchChangeTask = orderChangeBatch;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> ordersForCancelSL,
                                          final MergeCommand mergeCommand) {
        return Observable.defer(() -> batchChangeTask.cancelSL(ordersForCancelSL,
                                                               mergeCommand.orderCancelSLMode(),
                                                               mergeCommand::orderCancelSLComposer));
    }
}
