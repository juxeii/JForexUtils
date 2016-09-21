package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.PlatformSettings;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class OrderCancelSL {

    private final OrderTaskExecutor orderTaskExecutor;
    private final OrderUtilHandler orderUtilHandler;

    private static final PlatformSettings platformSettings = JForexUtil.platformSettings;

    public OrderCancelSL(final OrderTaskExecutor orderTaskExecutor,
                         final OrderUtilHandler orderUtilHandler) {
        this.orderTaskExecutor = orderTaskExecutor;
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> observeTask(final MergeCommandWithParent command) {
        return Observable.empty();
    }

    private Observable<OrderEvent> cancelSL(final Collection<IOrder> toMergeOrders,
                                            final MergeCommandWithParent command) {
        return batch(toMergeOrders, order -> orderTaskExecutor.setStopLossPrice(order, platformSettings.noSLPrice())
            .compose(command.cancelSLCompose(order)));
    }

    public final Observable<OrderEvent> batch(final Collection<IOrder> orders,
                                              final Function<IOrder, Observable<OrderEvent>> batchTask) {
        orderTaskExecutor.setStopLossPrice(order, platformSettings.noSLPrice()))
    .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_TP)));

        return Observable.defer(() -> Observable
            .fromIterable(orders)
            .flatMap(order -> orderTaskExecutor.setStopLossPrice(order, platformSettings.noSLPrice()))
            .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_TP)));
    }

    private Observable<OrderEvent> orderUtilObservable(final IOrder order,
                                                       final OrderCallReason orderCallReason) {
        return Observable.defer(() -> orderUtilHandler.callObservable(order, orderCallReason));
    }
}
