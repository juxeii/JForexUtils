package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.position.ClosePositionParams;

import io.reactivex.Observable;

public class MergeAndClosePositionTask {

    private final MergePositionTask mergePositionTask;
    private final BatchChangeTask batchChangeTask;
    final OrdersForPositionClose ordersForPositionClose;

    public MergeAndClosePositionTask(final MergePositionTask mergePositionTask,
                                     final BatchChangeTask batchChangeTask,
                                     final OrdersForPositionClose ordersForPositionClose) {
        this.mergePositionTask = mergePositionTask;
        this.batchChangeTask = batchChangeTask;
        this.ordersForPositionClose = ordersForPositionClose;
    }

    public Observable<OrderEvent> observeMerge(final ClosePositionParams closePositionParams) {
        return closePositionParams.closeExecutionMode() == CloseExecutionMode.CloseOpened
                ? Observable.empty()
                : observeMergeForFilledOrders(closePositionParams);
    }

    private Observable<OrderEvent> observeMergeForFilledOrders(final ClosePositionParams closePositionParams) {
        return Observable.defer(() -> {
            final Collection<IOrder> filledOrders = ordersForPositionClose.filled(closePositionParams.instrument());
            return filledOrders.size() > 1
                    ? mergePositionTask.merge(filledOrders, closePositionParams.mergePositionParams())
                    : Observable.empty();
        });
    }

    public Observable<OrderEvent> observeClose(final ClosePositionParams closePositionParams) {
        return Observable.defer(() -> {
            final Collection<IOrder> ordersToClose = ordersForPositionClose.forMode(closePositionParams);
            return ordersToClose.size() > 0
                    ? batchChangeTask.close(ordersToClose, closePositionParams)
                    : Observable.empty();
        });
    }
}
