package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionChangeTask;
import com.jforex.programming.position.PositionCreateTask;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.RestoreSLTPPolicy;

import rx.Completable;
import rx.Observable;

public class OrderUtil {

    private final PositionCreateTask positionCreateTask;
    private final PositionChangeTask positionChangeTask;
    private final OrderChangeUtil orderChangeUtil;
    private final PositionFactory positionFactory;

    public OrderUtil(final PositionCreateTask positionCreateTask,
                     final PositionChangeTask positionChangeTask,
                     final OrderChangeUtil orderChangeUtil,
                     final PositionFactory positionFactory) {
        this.positionCreateTask = positionCreateTask;
        this.positionChangeTask = positionChangeTask;
        this.orderChangeUtil = orderChangeUtil;
        this.positionFactory = positionFactory;
    }

    public Position position(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        return positionCreateTask.submitOrder(orderParams);
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return positionCreateTask.mergeOrders(mergeOrderLabel, toMergeOrders);
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument,
                                                      final RestoreSLTPPolicy restoreSLTPPolicy) {
        return positionCreateTask.mergePositionOrders(mergeOrderLabel, instrument, restoreSLTPPolicy);
    }

    public Completable closePosition(final Instrument instrument) {
        return positionChangeTask.closePosition(instrument);
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return orderChangeUtil.close(orderToClose);
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return orderChangeUtil.setLabel(orderToChangeLabel, newLabel);
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return orderChangeUtil.setGoodTillTime(orderToChangeGTT, newGTT);
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return orderChangeUtil.setOpenPrice(orderToChangeOpenPrice, newOpenPrice);
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeRequestedAmount,
                                                     final double newRequestedAmount) {
        return orderChangeUtil.setRequestedAmount(orderToChangeRequestedAmount, newRequestedAmount);
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return orderChangeUtil.setStopLossPrice(orderToChangeSL, newSL);
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return orderChangeUtil.setTakeProfitPrice(orderToChangeTP, newTP);
    }
}
