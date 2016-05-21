package com.jforex.programming.position.task;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.event.OrderEvent;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Observable;

public class PositionSingleTask {

    private final OrderCreateUtil orderCreateUtil;
    private final OrderChangeUtil orderChangeUtil;
    private final PositionRetryTask<IOrder> orderRetryTask;
    private final PositionRetryTask<String> mergeRetryTask;

    private static final Logger logger = LogManager.getLogger(PositionSingleTask.class);

    public PositionSingleTask(final OrderCreateUtil orderCreateUtil,
                              final OrderChangeUtil orderChangeUtil,
                              final PositionRetryTask<IOrder> orderRetryTask,
                              final PositionRetryTask<String> mergeRetryTask) {
        this.orderCreateUtil = orderCreateUtil;
        this.orderChangeUtil = orderChangeUtil;
        this.orderRetryTask = orderRetryTask;
        this.mergeRetryTask = mergeRetryTask;
    }

    public Observable<OrderEvent> setSLObservable(final IOrder orderToChangeSL,
                                                  final double newSL) {
        final double currentSL = orderToChangeSL.getStopLossPrice();
        return orderRetryTask.create(() -> orderChangeUtil.setStopLossPrice(orderToChangeSL, newSL),
                                     order -> !isSLSetTo(newSL).test(order),
                                     orderToChangeSL)
                .doOnSubscribe(() -> logger.debug("Start to change SL from " + currentSL + " to "
                        + newSL + " for order " + orderToChangeSL.getLabel() + " and position "
                        + orderToChangeSL.getInstrument()))
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
        return orderRetryTask.create(() -> orderChangeUtil.setTakeProfitPrice(orderToChangeTP, newTP),
                                     order -> !isTPSetTo(newTP).test(order),
                                     orderToChangeTP)
                .doOnSubscribe(() -> logger.debug("Start to change TP from " + currentTP + " to "
                        + newTP + " for order " + orderToChangeTP.getLabel() + " and position "
                        + orderToChangeTP.getInstrument()))
                .doOnError(e -> logger.debug("Failed to change TP from " + currentTP + " to " + newTP +
                        " for order " + orderToChangeTP.getLabel() + " and position "
                        + orderToChangeTP.getInstrument() + ".Excpetion: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Changed TP from " + currentTP + " to " + newTP +
                        " for order " + orderToChangeTP.getLabel() + " and position "
                        + orderToChangeTP.getInstrument()));
    }

    public Observable<OrderEvent> closeObservable(final IOrder orderToClose) {
        return orderRetryTask.create(() -> orderChangeUtil.close(orderToClose),
                                     order -> !isClosed.test(order),
                                     orderToClose)
                .doOnSubscribe(() -> logger.debug("Starting to close order " + orderToClose.getLabel()
                        + " for " + orderToClose.getInstrument() + " position."))
                .doOnError(e -> logger.error("Closing position " + orderToClose.getInstrument()
                        + " failed! Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Closing position "
                        + orderToClose.getInstrument() + " was successful."));
    }

    public Observable<OrderEvent> mergeObservable(final String mergeOrderLabel,
                                                  final Collection<IOrder> toMergeOrders) {
        final Instrument instrument = toMergeOrders.iterator().next().getInstrument();
        return mergeRetryTask.create(() -> orderCreateUtil.mergeOrders(mergeOrderLabel, toMergeOrders),
                                     order -> true,
                                     mergeOrderLabel)
                .doOnSubscribe(() -> logger.debug("Starting to merge with label " + mergeOrderLabel
                        + " for " + instrument + " position."))
                .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel
                        + " for " + instrument + " failed! Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.debug("Merging with label " + mergeOrderLabel
                        + " for " + instrument + " position was successful."));
    }
}
