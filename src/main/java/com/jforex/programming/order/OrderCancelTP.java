package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class OrderCancelTP {

    private final OrderChangeBatch orderChangeBatch;

    public OrderCancelTP(final OrderChangeBatch orderChangeBatch) {
        this.orderChangeBatch = orderChangeBatch;
    }

    public Observable<OrderEvent> observeTask(final Collection<IOrder> toCancelTPOrders,
                                              final MergeCommand command) {
        return Observable.defer(() -> orderChangeBatch
            .cancelSL(toCancelTPOrders, command::singleCancelTPCompose));
    }
}
