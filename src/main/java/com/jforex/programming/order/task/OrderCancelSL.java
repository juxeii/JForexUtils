package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class OrderCancelSL {

    private final OrderChangeBatch orderChangeBatch;

    public OrderCancelSL(final OrderChangeBatch orderChangeBatch) {
        this.orderChangeBatch = orderChangeBatch;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> ordersForCancelSL,
                                          final MergeCommand mergeCommand) {
        return Observable.defer(() -> orderChangeBatch.cancelSL(ordersForCancelSL,
                                                                mergeCommand.orderCancelSLMode(),
                                                                mergeCommand::orderCancelSLComposer));
    }
}
