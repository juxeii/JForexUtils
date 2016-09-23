package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.MergeCommand.MergeExecutionMode;
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

    public Observable<OrderEvent> observeTask(final Collection<IOrder> toCancelSLTPOrders,
                                              final MergeCommand command) {
        return Observable.defer(() -> toCancelSLTPOrders.size() < 2
                ? Observable.empty()
                : createTask(toCancelSLTPOrders, command));
    }

    private Observable<OrderEvent> createTask(final Collection<IOrder> toCancelSLTPOrders,
                                              final MergeCommand command) {
        final Observable<OrderEvent> cancelSL = orderCancelSL.observeTask(toCancelSLTPOrders, command);
        final Observable<OrderEvent> cancelTP = orderCancelTP.observeTask(toCancelSLTPOrders, command);

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
