package com.jforex.programming.order;

import java.util.Collection;
import java.util.Set;

import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergePositionCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.RestoreSLTPData;
import com.jforex.programming.position.RestoreSLTPPolicy;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Completable;
import rx.Observable;

public class OrderUtil {

    private final IEngine engine;
    private final OrderPositionHandler orderPositionHandler;
    private final OrderUtilHandler orderUtilHandler;

    public OrderUtil(final IEngine engine,
                     final OrderPositionHandler orderPositionHandler,
                     final OrderUtilHandler orderUtilHandler) {
        this.engine = engine;
        this.orderPositionHandler = orderPositionHandler;
        this.orderUtilHandler = orderUtilHandler;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return orderPositionHandler.positionOrders(instrument);
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        return orderPositionHandler.submitOrder(new SubmitCommand(orderParams, engine));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        return orderPositionHandler
                .mergeOrders(new MergeCommand(mergeOrderLabel, toMergeOrders, engine));
    }

    public Observable<OrderEvent> mergePositionOrders(final String mergeOrderLabel,
                                                      final Instrument instrument,
                                                      final RestoreSLTPPolicy restoreSLTPPolicy) {
        final Set<IOrder> toMergeOrders = positionOrders(instrument).filled();
        if (toMergeOrders.size() < 2)
            return Observable.empty();

        final RestoreSLTPData restoreSLTPData =
                new RestoreSLTPData(restoreSLTPPolicy.restoreSL(toMergeOrders),
                                    restoreSLTPPolicy.restoreTP(toMergeOrders));
        final MergePositionCommand command = new MergePositionCommand(mergeOrderLabel,
                                                                      toMergeOrders,
                                                                      instrument,
                                                                      restoreSLTPData,
                                                                      engine);
        return orderPositionHandler.mergePositionOrders(command);
    }

    public Completable closePosition(final Instrument instrument) {
        return orderPositionHandler.closePosition(instrument);
    }

    public Observable<OrderEvent> close(final IOrder orderToClose) {
        return orderUtilHandler.observable(OrderCallCommand.closeCommand(orderToClose));
    }

    public Observable<OrderEvent> setLabel(final IOrder orderToChangeLabel,
                                           final String newLabel) {
        return orderUtilHandler
                .observable(OrderCallCommand.setLabelCommand(orderToChangeLabel, newLabel));
    }

    public Observable<OrderEvent> setGoodTillTime(final IOrder orderToChangeGTT,
                                                  final long newGTT) {
        return orderUtilHandler.observable(OrderCallCommand.setGTTCommand(orderToChangeGTT, newGTT));
    }

    public Observable<OrderEvent> setOpenPrice(final IOrder orderToChangeOpenPrice,
                                               final double newOpenPrice) {
        return orderUtilHandler
                .observable(OrderCallCommand.setOpenPriceCommand(orderToChangeOpenPrice, newOpenPrice));
    }

    public Observable<OrderEvent> setRequestedAmount(final IOrder orderToChangeAmount,
                                                     final double newRequestedAmount) {
        return orderUtilHandler
                .observable(OrderCallCommand.setAmountCommand(orderToChangeAmount, newRequestedAmount));
    }

    public Observable<OrderEvent> setStopLossPrice(final IOrder orderToChangeSL,
                                                   final double newSL) {
        return orderUtilHandler.observable(OrderCallCommand.setSLCommand(orderToChangeSL, newSL));
    }

    public Observable<OrderEvent> setTakeProfitPrice(final IOrder orderToChangeTP,
                                                     final double newTP) {
        return orderUtilHandler.observable(OrderCallCommand.setTPCommand(orderToChangeTP, newTP));
    }
}
