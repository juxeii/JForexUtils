package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;

import io.reactivex.Observable;

public final class OrderUtil {

    private final OrderTaskExecutor orderTaskExecutor;
    private final OrderUtilHandler orderUtilHandler;
    private final PositionFactory positionFactory;

    public OrderUtil(final OrderTaskExecutor orderTaskExecutor,
                     final OrderUtilHandler orderUtilHandler,
                     final PositionFactory positionFactory) {
        this.orderTaskExecutor = orderTaskExecutor;
        this.orderUtilHandler = orderUtilHandler;
        this.positionFactory = positionFactory;
    }

    public final Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        return orderTaskExecutor
            .submitOrder(orderParams)
            .toObservable()
            .flatMap(order -> orderUtilHandler.callObservable(order, OrderCallReason.SUBMIT));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return orderTaskExecutor
            .mergeOrders(mergeOrderLabel, toMergeOrders)
            .toObservable()
            .flatMap(order -> orderUtilHandler.callObservable(order, OrderCallReason.MERGE));
    }

    public Observable<OrderEvent> close(final IOrder order) {
        return orderTaskExecutor
            .close(order)
            .andThen(orderUtilHandler.callObservable(order, OrderCallReason.CLOSE));
    }

    public Observable<OrderEvent> setLabel(final IOrder order,
                                           final String label) {
        return orderTaskExecutor
            .setLabel(order, label)
            .andThen(orderUtilHandler.callObservable(order, OrderCallReason.CHANGE_LABEL));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder order,
                                                  final long newGTT) {
        return orderTaskExecutor
            .setGoodTillTime(order, newGTT)
            .andThen(orderUtilHandler.callObservable(order, OrderCallReason.CHANGE_GTT));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder order,
                                                     final double newRequestedAmount) {
        return orderTaskExecutor
            .setRequestedAmount(order, newRequestedAmount)
            .andThen(orderUtilHandler.callObservable(order, OrderCallReason.CHANGE_AMOUNT));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder order,
                                               final double newOpenPrice) {
        return orderTaskExecutor
            .setOpenPrice(order, newOpenPrice)
            .andThen(orderUtilHandler.callObservable(order, OrderCallReason.CHANGE_PRICE));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder order,
                                                   final double newSL) {
        return orderTaskExecutor
            .setStopLossPrice(order, newSL)
            .andThen(orderUtilHandler.callObservable(order, OrderCallReason.CHANGE_SL));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder order,
                                                     final double newTP) {
        return orderTaskExecutor
            .setTakeProfitPrice(order, newTP)
            .andThen(orderUtilHandler.callObservable(order, OrderCallReason.CHANGE_TP));
    }

    public final PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(checkNotNull(instrument));
    }
}
