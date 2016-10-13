package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergeCommandHandler;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class CancelSLTPAndMergeTask {

    private final MergeCommandHandler mergeCommandHandler;

    public CancelSLTPAndMergeTask(final MergeCommandHandler mergeCommandHandler) {
        this.mergeCommandHandler = mergeCommandHandler;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toMergeOrders,
                                          final MergeCommand command) {
        final Observable<OrderEvent> cancelSLTP = mergeCommandHandler.observeCancelSLTP(toMergeOrders, command);
        final Observable<OrderEvent> merge = mergeCommandHandler.observeMerge(toMergeOrders, command);

        return cancelSLTP.concatWith(merge);
    }
}
