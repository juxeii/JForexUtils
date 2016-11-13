package com.jforex.programming.order.task.params.position;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.MergePositionTask;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;

public class ClosePositionParamsHandler {

    private final MergePositionTask mergePositionTask;
    private final BatchChangeTask batchChangeTask;
    private final PositionUtil positionUtil;

    public ClosePositionParamsHandler(final MergePositionTask mergePositionTask,
                                      final BatchChangeTask batchChangeTask,
                                      final PositionUtil positionUtil) {
        this.mergePositionTask = mergePositionTask;
        this.batchChangeTask = batchChangeTask;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> observeMerge(final ClosePositionParams closePositionParams) {
        return closePositionParams.closeExecutionMode() == CloseExecutionMode.CloseOpened
                ? Observable.empty()
                : observeMergeForFilledOrders(closePositionParams);
    }

    private Observable<OrderEvent> observeMergeForFilledOrders(final ClosePositionParams closePositionParams) {
        return Observable.defer(() -> {
            final Collection<IOrder> ordersToMerge = positionUtil.filledOrders(closePositionParams.instrument());
            return mergePositionTask.merge(ordersToMerge, closePositionParams.mergePositionParams());
        });
    }

    public Observable<OrderEvent> observeClose(final ClosePositionParams closePositionParams) {
        return Observable.defer(() -> batchChangeTask.close(ordersToClose(closePositionParams), closePositionParams));
    }

    private Collection<IOrder> ordersToClose(final ClosePositionParams closePositionParams) {
        final Instrument instrument = closePositionParams.instrument();
        final CloseExecutionMode closeExecutionMode = closePositionParams.closeExecutionMode();

        if (closeExecutionMode == CloseExecutionMode.CloseFilled)
            return positionUtil.filledOrders(instrument);
        if (closeExecutionMode == CloseExecutionMode.CloseOpened)
            return positionUtil.openedOrders(instrument);
        return positionUtil.filledOrOpenedOrders(instrument);
    }
}
