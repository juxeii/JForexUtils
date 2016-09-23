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

    public Observable<OrderEvent> observe(final Collection<IOrder> toCancelTPOrders,
                                          final MergeCommand mergeCommand) {
        return Observable.defer(() -> orderChangeBatch
            .cancelTP(toCancelTPOrders, mergeCommand::orderCancelTPComposer));
    }
}
