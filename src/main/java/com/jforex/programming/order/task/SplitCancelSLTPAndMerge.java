package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergeCommandHandler;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class SplitCancelSLTPAndMerge {

    private final MergeCommandHandler commandHandler;

    public SplitCancelSLTPAndMerge(final MergeCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toMergeOrders,
                                          final MergeCommand command) {
        final Observable<OrderEvent> cancelSLTP = commandHandler.observeCancelSLTP(toMergeOrders, command);
        final Observable<OrderEvent> merge = commandHandler.observeMerge(toMergeOrders, command);

        return cancelSLTP.concatWith(merge);
    }
}
