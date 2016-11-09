package com.jforex.programming.order.task.params.position;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.MergePositionTaskObservable;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;

public class ClosePositionParamsHandler {

    private final MergePositionTaskObservable mergePositionTask;
    private final BatchChangeTask batchChangeTask;
    private final PositionUtil positionUtil;

    public ClosePositionParamsHandler(final MergePositionTaskObservable mergePositionTask,
                                      final BatchChangeTask batchChangeTask,
                                      final PositionUtil positionUtil) {
        this.mergePositionTask = mergePositionTask;
        this.batchChangeTask = batchChangeTask;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> observeMerge(final Instrument instrument,
                                               final ClosePositionParams closePositionParams) {
        return closePositionParams.closeExecutionMode() == CloseExecutionMode.CloseOpened
                ? Observable.empty()
                : observeMergeForFilledOrders(instrument, closePositionParams);
    }

    private Observable<OrderEvent> observeMergeForFilledOrders(final Instrument instrument,
                                                               final ClosePositionParams closePositionParams) {
        return Observable.defer(() -> {
            final Collection<IOrder> ordersToMerge = positionUtil.filledOrders(instrument);
            return mergePositionTask.merge(ordersToMerge,
                                           closePositionParams.mergePositionParams());
        });
    }

    public Observable<OrderEvent> observeClose(final Instrument instrument,
                                               final ClosePositionParams closePositionParams) {
        return Observable.defer(() -> batchChangeTask.close(instrument,
                                                            ordersToClose(instrument, closePositionParams),
                                                            closePositionParams.simpleClosePositionParams()));
    }

    private Collection<IOrder> ordersToClose(final Instrument instrument,
                                             final ClosePositionParams closePositionParams) {
        final CloseExecutionMode closeExecutionMode = closePositionParams.closeExecutionMode();

        if (closeExecutionMode == CloseExecutionMode.CloseFilled)
            return positionUtil.filledOrders(instrument);
        if (closeExecutionMode == CloseExecutionMode.CloseOpened)
            return positionUtil.openedOrders(instrument);
        return positionUtil.filledOrOpenedOrders(instrument);
    }
}
