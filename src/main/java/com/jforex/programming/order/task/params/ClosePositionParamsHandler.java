package com.jforex.programming.order.task.params;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;

public class ClosePositionParamsHandler {

    private final MergeTask orderMergeTask;
    private final BatchChangeTask batchChangeTask;
    private final PositionUtil positionUtil;

    public ClosePositionParamsHandler(final MergeTask orderMergeTask,
                                      final BatchChangeTask orderChangeBatch,
                                      final PositionUtil positionUtil) {
        this.orderMergeTask = orderMergeTask;
        this.batchChangeTask = orderChangeBatch;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> observeMerge(final Instrument instrument,
                                               final ClosePositionParams positionParams) {
        return positionParams.executionMode() == CloseExecutionMode.CloseOpened
                ? Observable.empty()
                : observeMergeForFilledOrders(instrument, positionParams);
    }

    private Observable<OrderEvent> observeMergeForFilledOrders(final Instrument instrument,
                                                               final ClosePositionParams positionParams) {
        return Observable.defer(() -> {
            final Collection<IOrder> ordersToMerge = positionUtil.filledOrders(instrument);
            final MergePositionParams mergepositionParams = positionParams.maybeMergeParams().get();

            return orderMergeTask.merge(ordersToMerge, mergepositionParams);
        });
    }

    public Observable<OrderEvent> observeClose(final Instrument instrument,
                                               final ClosePositionParams positionParams) {
        return Observable.defer(() -> {
            final Collection<IOrder> ordersToClose = ordersToClose(instrument, positionParams);
            final Observable<OrderEvent> batchClose =
                    batchChangeTask.close(ordersToClose,
                                          positionParams.closeParamsProvider(),
                                          positionParams.closeBatchMode(),
                                          positionParams::singleCloseComposer);
            return composeBatchClose(batchClose, positionParams);
        });
    }

    private Collection<IOrder> ordersToClose(final Instrument instrument,
                                             final ClosePositionParams positionParams) {
        final CloseExecutionMode executionMode = positionParams.executionMode();

        if (executionMode == CloseExecutionMode.CloseFilled)
            return positionUtil.filledOrders(instrument);
        if (executionMode == CloseExecutionMode.CloseOpened)
            return positionUtil.openedOrders(instrument);
        return positionUtil.filledOrOpenedOrders(instrument);
    }

    private Observable<OrderEvent> composeBatchClose(final Observable<OrderEvent> batchClose,
                                                     final ClosePositionParams positionParams) {
        final CloseExecutionMode executionMode = positionParams.executionMode();

        if (executionMode == CloseExecutionMode.CloseFilled)
            return batchClose.compose(positionParams.closeFilledComposer());
        if (executionMode == CloseExecutionMode.CloseOpened)
            return batchClose.compose(positionParams.closeOpenedComposer());
        return batchClose.compose(positionParams.closeAllComposer());
    }
}
