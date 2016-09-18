package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.PlatformSettings;

import io.reactivex.Observable;

public class OrderTask {

    private final OrderTaskExecutor orderTaskExecutor;
    private final OrderUtilHandler orderUtilHandler;

    private static final PlatformSettings platformSettings = JForexUtil.platformSettings;

    public OrderTask(final OrderTaskExecutor orderTaskExecutor,
                     final OrderUtilHandler orderUtilHandler) {
        this.orderTaskExecutor = orderTaskExecutor;
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        return orderTaskExecutor
            .submitOrder(orderParams)
            .toObservable()
            .flatMap(order -> orderUtilObservable(order, OrderCallReason.SUBMIT));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return orderTaskExecutor
            .mergeOrders(mergeOrderLabel, toMergeOrders)
            .toObservable()
            .flatMap(order -> orderUtilObservable(order, OrderCallReason.MERGE));
    }

    public Observable<OrderEvent> close(final IOrder order) {
        return orderTaskExecutor
            .close(order)
            .andThen(orderUtilObservable(order, OrderCallReason.CLOSE));
    }

    public Observable<OrderEvent> setLabel(final IOrder order,
                                           final String label) {
        return orderTaskExecutor
            .setLabel(order, label)
            .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_LABEL));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder order,
                                                  final long newGTT) {
        return orderTaskExecutor
            .setGoodTillTime(order, newGTT)
            .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_GTT));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder order,
                                                     final double newRequestedAmount) {
        return orderTaskExecutor
            .setRequestedAmount(order, newRequestedAmount)
            .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_AMOUNT));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder order,
                                               final double newOpenPrice) {
        return orderTaskExecutor
            .setOpenPrice(order, newOpenPrice)
            .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_PRICE));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder order,
                                                   final double newSL) {
        return orderTaskExecutor
            .setStopLossPrice(order, newSL)
            .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_SL));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder order,
                                                     final double newTP) {
        return orderTaskExecutor
            .setTakeProfitPrice(order, newTP)
            .andThen(orderUtilObservable(order, OrderCallReason.CHANGE_TP));
    }

    public Observable<OrderEvent> cancelStopLossPrice(final IOrder order) {
        return setStopLossPrice(order, platformSettings.noSLPrice());
    }

    public Observable<OrderEvent> cancelTakeProfitPrice(final IOrder order) {
        return setTakeProfitPrice(order, platformSettings.noTPPrice());
    }

    private Observable<OrderEvent> orderUtilObservable(final IOrder order,
                                                       final OrderCallReason orderCallReason) {
        return Observable.defer(() -> orderUtilHandler.callObservable(order, orderCallReason));
    }
}
