package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.StreamUtil;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public class PositionSingleTask {

    private final OrderCreateUtil orderCreateUtil;
    private final OrderChangeUtil orderChangeUtil;

    private static final Logger logger = LogManager.getLogger(PositionSingleTask.class);

    public PositionSingleTask(final OrderCreateUtil orderCreateUtil,
                              final OrderChangeUtil orderChangeUtil) {
        this.orderCreateUtil = orderCreateUtil;
        this.orderChangeUtil = orderChangeUtil;
    }

    public Observable<OrderEvent> setSLObservable(final IOrder orderToChangeSL,
                                                  final double newSL) {
        final double currentSL = orderToChangeSL.getStopLossPrice();

        return Observable
                .just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(order))
                .doOnNext(order -> logger.debug("Start to change SL from " + currentSL + " to "
                        + newSL + " for order " + order.getLabel() + " and position "
                        + order.getInstrument()))
                .flatMap(order -> orderChangeUtil.setStopLossPrice(order, newSL))
                .retryWhen(StreamUtil::positionTaskRetry)
                .doOnError(e -> logger.debug("Failed to change SL from " + currentSL + " to " + newSL +
                        " for order " + orderToChangeSL.getLabel() + " and position "
                        + orderToChangeSL.getInstrument() + ".Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Changed SL from " + currentSL + " to " + newSL +
                        " for order " + orderToChangeSL.getLabel() + " and position "
                        + orderToChangeSL.getInstrument()));
    }

    public Observable<OrderEvent> setTPObservable(final IOrder orderToChangeTP,
                                                  final double newTP) {
        final double currentTP = orderToChangeTP.getTakeProfitPrice();

        return Observable
                .just(orderToChangeTP)
                .filter(order -> !isTPSetTo(newTP).test(order))
                .doOnNext(order -> logger.debug("Start to change TP from " + currentTP + " to "
                        + newTP + " for order " + order.getLabel() + " and position "
                        + order.getInstrument()))
                .flatMap(order -> orderChangeUtil.setTakeProfitPrice(order, newTP))
                .retryWhen(StreamUtil::positionTaskRetry)
                .doOnError(e -> logger.debug("Failed to change TP from " + currentTP + " to " + newTP +
                        " for order " + orderToChangeTP.getLabel() + " and position "
                        + orderToChangeTP.getInstrument() + ".Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Changed TP from " + currentTP + " to " + newTP +
                        " for order " + orderToChangeTP.getLabel() + " and position "
                        + orderToChangeTP.getInstrument()));
    }

    public Observable<OrderEvent> mergeObservable(final String mergeOrderLabel,
                                                  final Collection<IOrder> toMergeOrders) {
        final Instrument instrument = toMergeOrders.iterator().next().getInstrument();

        return Observable
                .just(mergeOrderLabel)
                .doOnSubscribe(() -> logger.debug("Starting to merge with label " + mergeOrderLabel
                        + " for " + instrument + " position."))
                .flatMap(order -> orderCreateUtil.mergeOrders(mergeOrderLabel, toMergeOrders))
                .retryWhen(StreamUtil::positionTaskRetry)
                .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel
                        + " for " + instrument + " failed! Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Merging with label " + mergeOrderLabel
                        + " for " + instrument + " position was successful."));
    }

    public Observable<OrderEvent> closeObservable(final IOrder orderToClose) {
        return Observable
                .just(orderToClose)
                .filter(order -> !isClosed.test(order))
                .doOnNext(order -> logger.debug("Starting to close order " + order.getLabel()
                        + " for " + order.getInstrument() + " position."))
                .flatMap(orderChangeUtil::close)
                .retryWhen(StreamUtil::positionTaskRetry)
                .doOnError(e -> logger.error("Closing position " + orderToClose.getInstrument()
                        + " failed! Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Closing position "
                        + orderToClose.getInstrument() + " was successful."));
    }
}
