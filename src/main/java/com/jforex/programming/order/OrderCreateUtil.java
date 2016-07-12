package com.jforex.programming.order;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Observable;

public class OrderCreateUtil {

    private final IEngine engine;
    private final OrderUtilHandler orderUtilHandler;

    private static final Logger logger = LogManager.getLogger(OrderCreateUtil.class);

    public OrderCreateUtil(final IEngine engine,
                           final OrderUtilHandler orderUtilHandler) {
        this.engine = engine;
        this.orderUtilHandler = orderUtilHandler;
    }

    public Observable<OrderEvent> submitOrder(final OrderParams orderParams) {
        final Instrument instrument = orderParams.instrument();
        final String orderLabel = orderParams.label();
        final Callable<IOrder> submitCall = () -> engine.submitOrder(orderParams.label(),
                                                                     instrument,
                                                                     orderParams.orderCommand(),
                                                                     orderParams.amount(),
                                                                     orderParams.price(),
                                                                     orderParams.slippage(),
                                                                     orderParams.stopLossPrice(),
                                                                     orderParams.takeProfitPrice(),
                                                                     orderParams.goodTillTime(),
                                                                     orderParams.comment());

        return orderUtilHandler
                .submitObservable(submitCall, OrderEventTypeData.submitData)
                .doOnSubscribe(() -> logger.debug("Start submit task with label "
                        + orderLabel + " for " + instrument))
                .doOnError(e -> logger.error("Submit task with label " + orderLabel + " for "
                        + instrument + " failed! Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.error("Submit task with label " + orderLabel + " for "
                        + instrument + " was successful."));
    }

    public Observable<OrderEvent> mergeOrders(final String mergeOrderLabel,
                                              final Collection<IOrder> toMergeOrders) {
        final Callable<IOrder> mergeCall = () -> engine.mergeOrders(mergeOrderLabel, toMergeOrders);
        final Instrument instrument = toMergeOrders.iterator().next().getInstrument();

        return orderUtilHandler
                .submitObservable(mergeCall, OrderEventTypeData.mergeData)
                .doOnSubscribe(() -> logger.debug("Starting to merge with label " + mergeOrderLabel
                        + " for position " + instrument + "."))
                .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel
                        + " for position " + instrument + " failed! Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Merging with label " + mergeOrderLabel
                        + " for position " + instrument + " was successful."));
    }
}
