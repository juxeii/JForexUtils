package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class CancelSLTPTask {

    private final CancelSLTask cancelSLTask;
    private final CancelTPTask cancelTPTask;

    public CancelSLTPTask(final CancelSLTask orderCancelSL,
                          final CancelTPTask orderCancelTP) {
        this.cancelSLTask = orderCancelSL;
        this.cancelTPTask = orderCancelTP;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelSLTPOrders,
                                          final MergeCommand command) {
        return Observable.defer(() -> toCancelSLTPOrders.size() < 2
                ? Observable.empty()
                : createTask(toCancelSLTPOrders, command));
    }

    private Observable<OrderEvent> createTask(final Collection<IOrder> toCancelSLTPOrders,
                                              final MergeCommand command) {
        final Observable<OrderEvent> cancelSL = cancelSLTask.observe(toCancelSLTPOrders, command);
        final Observable<OrderEvent> cancelTP = cancelTPTask.observe(toCancelSLTPOrders, command);

        return arrangeObservables(cancelSL, cancelTP, command.executionMode())
            .compose(command.cancelSLTPComposer());
    }

    private Observable<OrderEvent> arrangeObservables(final Observable<OrderEvent> cancelSL,
                                                      final Observable<OrderEvent> cancelTP,
                                                      final MergeExecutionMode executionMode) {
        if (executionMode == MergeExecutionMode.ConcatCancelSLAndTP)
            return cancelSL.concatWith(cancelTP);
        else if (executionMode == MergeExecutionMode.ConcatCancelTPAndSL)
            return cancelTP.concatWith(cancelSL);
        return cancelSL.mergeWith(cancelTP);
    }
}
