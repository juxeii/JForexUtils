package com.jforex.programming.order.command;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Observable;

public class ClosePositionCommandHandler {

    private final MergeTask orderMergeTask;
    private final BatchChangeTask batchChangeTask;
    private final PositionUtil positionUtil;

    public ClosePositionCommandHandler(final MergeTask orderMergeTask,
                                       final BatchChangeTask orderChangeBatch,
                                       final PositionUtil positionUtil) {
        this.orderMergeTask = orderMergeTask;
        this.batchChangeTask = orderChangeBatch;
        this.positionUtil = positionUtil;
    }

    public Observable<OrderEvent> observeMerge(final ClosePositionCommand command) {
        return command.executionMode() == CloseExecutionMode.CloseOpened
                ? Observable.empty()
                : observeMergeForFilledOrders(command);
    }

    private Observable<OrderEvent> observeMergeForFilledOrders(final ClosePositionCommand command) {
        return Observable.defer(() -> {
            final Instrument instrument = command.instrument();
            final Collection<IOrder> ordersToMerge = positionUtil.filledOrders(instrument);
            final MergeCommand mergeCommand = command.maybeMergeCommand().get();

            return orderMergeTask.merge(ordersToMerge, mergeCommand);
        });
    }

    public Observable<OrderEvent> observeClose(final ClosePositionCommand command) {
        return Observable.defer(() -> {
            final Collection<IOrder> ordersToClose = ordersToClose(command);
            final Observable<OrderEvent> batchClose =
                    batchChangeTask.close(ordersToClose,
                                          command.closeParamsProvider(),
                                          command.closeBatchMode(),
                                          command::singleCloseComposer);
            return composeBatchClose(batchClose, command);
        });
    }

    private Collection<IOrder> ordersToClose(final ClosePositionCommand command) {
        final CloseExecutionMode executionMode = command.executionMode();
        final Instrument instrument = command.instrument();

        if (executionMode == CloseExecutionMode.CloseFilled)
            return positionUtil.filledOrders(instrument);
        if (executionMode == CloseExecutionMode.CloseOpened)
            return positionUtil.openedOrders(instrument);
        return positionUtil.filledOrOpenedOrders(instrument);
    }

    private Observable<OrderEvent> composeBatchClose(final Observable<OrderEvent> batchClose,
                                                     final ClosePositionCommand command) {
        final CloseExecutionMode executionMode = command.executionMode();

        if (executionMode == CloseExecutionMode.CloseFilled)
            return batchClose.compose(command.closeFilledComposer());
        if (executionMode == CloseExecutionMode.CloseOpened)
            return batchClose.compose(command.closeOpenedComposer());
        return batchClose.compose(command.closeAllComposer());
    }
}
