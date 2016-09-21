package com.jforex.programming.order;

import com.jforex.programming.order.MergeCommandWithParent.MergeExecutionMode;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class OrderCancelSLAndTP {

    private final OrderCancelSL orderCancelSL;
    private final OrderCancelTP orderCancelTP;

    public OrderCancelSLAndTP(final OrderCancelSL orderCancelSL,
                              final OrderCancelTP orderCancelTP) {
        this.orderCancelSL = orderCancelSL;
        this.orderCancelTP = orderCancelTP;
    }

    public Observable<OrderEvent> observeTask(final MergeCommand command) {
        return command.toMergeOrders().size() < 2
                ? Observable.empty()
                : createTask(command.mergeCommandWithParent());
    }

    private Observable<OrderEvent> createTask(final MergeCommandWithParent command) {
        final Observable<OrderEvent> cancelSL = orderCancelSL.observeTask(command);
        final Observable<OrderEvent> cancelTP = orderCancelTP.observeTask(command);

        return arrangeObservables(cancelSL, cancelTP, command.executionMode())
            .compose(command.cancelSLTPCompose());
    }

    private Observable<OrderEvent> arrangeObservables(final Observable<OrderEvent> cancelSL,
                                                      final Observable<OrderEvent> cancelTP,
                                                      final MergeExecutionMode executionMode) {
        if (executionMode == MergeExecutionMode.ConcatSLAndTP)
            return cancelSL.concatWith(cancelTP);
        else if (executionMode == MergeExecutionMode.ConcatTPAndSL)
            return cancelTP.concatWith(cancelSL);
        return cancelSL.mergeWith(cancelTP);
    }
}
