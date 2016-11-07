package com.jforex.programming.order.task.params.position;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.ComplexMergeTask;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;

public class ClosePositionParamsHandler {

    private final ComplexMergeTask complexMergeTask;
    private final BatchChangeTask batchChangeTask;
    private final PositionUtil positionUtil;

    public ClosePositionParamsHandler(final ComplexMergeTask complexMergeTask,
                                      final BatchChangeTask orderChangeBatch,
                                      final PositionUtil positionUtil) {
        this.complexMergeTask = complexMergeTask;
        this.batchChangeTask = orderChangeBatch;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> observeMerge(final Instrument instrument,
                                               final ComplexClosePositionParams complexClosePositionParams) {
        return complexClosePositionParams.closeExecutionMode() == CloseExecutionMode.CloseOpened
                ? Observable.empty()
                : observeMergeForFilledOrders(instrument, complexClosePositionParams);
    }

    private Observable<OrderEvent> observeMergeForFilledOrders(final Instrument instrument,
                                                               final ComplexClosePositionParams complexClosePositionParams) {
        return Observable.defer(() -> {
            final Collection<IOrder> ordersToMerge = positionUtil.filledOrders(instrument);
            return complexMergeTask.merge(ordersToMerge,
                                          complexClosePositionParams.complexMergePositionParams());
        });
    }

    public Observable<OrderEvent> observeClose(final Instrument instrument,
                                               final ComplexClosePositionParams complexClosePositionParams) {
        return Observable.defer(() -> batchChangeTask.close(instrument,
                                                            ordersToClose(instrument, complexClosePositionParams),
                                                            complexClosePositionParams.closePositionParams()));
    }

    private Collection<IOrder> ordersToClose(final Instrument instrument,
                                             final ComplexClosePositionParams complexClosePositionParams) {
        final CloseExecutionMode closeExecutionMode = complexClosePositionParams.closeExecutionMode();

        if (closeExecutionMode == CloseExecutionMode.CloseFilled)
            return positionUtil.filledOrders(instrument);
        if (closeExecutionMode == CloseExecutionMode.CloseOpened)
            return positionUtil.openedOrders(instrument);
        return positionUtil.filledOrOpenedOrders(instrument);
    }
}
