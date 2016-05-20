package com.jforex.programming.position.task;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.position.PositionRetryLogic;

import rx.Completable;
import rx.Observable;

public class PositionSetSLTPTask {

    private final OrderChangeUtil orderChangeUtil;
    private final PositionRetryLogic positionRetryLogic;

    private static final Logger logger = LogManager.getLogger(PositionSetSLTPTask.class);

    public PositionSetSLTPTask(final OrderChangeUtil orderChangeUtil,
                               final PositionRetryLogic positionRetryLogic) {
        this.orderChangeUtil = orderChangeUtil;
        this.positionRetryLogic = positionRetryLogic;
    }

    public Completable setSLCompletable(final IOrder orderToChangeSL,
                                        final double newSL) {
        final double currentSL = orderToChangeSL.getStopLossPrice();
        return Observable.just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(orderToChangeSL))
                .doOnNext(order -> logger.debug("Start to change SL from " + currentSL + " to "
                        + newSL + " for order " + order.getLabel() + " and position "
                        + orderToChangeSL.getInstrument()))
                .flatMap(order -> orderChangeUtil.setStopLossPrice(order, newSL))
                .retryWhen(positionRetryLogic::shouldRetry)
                .doOnNext(orderEvent -> logger.debug("Changed SL from " + currentSL + " to " + newSL +
                        " for order " + orderToChangeSL.getLabel() + " and position "
                        + orderToChangeSL.getInstrument()))
                .toCompletable();
    }

    public Completable setTPCompletable(final IOrder orderToChangeTP,
                                        final double newTP) {
        final double currentTP = orderToChangeTP.getTakeProfitPrice();
        return Observable.just(orderToChangeTP)
                .filter(order -> !isTPSetTo(newTP).test(orderToChangeTP))
                .doOnNext(order -> logger.debug("Start to change TP from " + currentTP + " to "
                        + newTP + " for order " + order.getLabel() + " and position "
                        + orderToChangeTP.getInstrument()))
                .flatMap(order -> orderChangeUtil.setTakeProfitPrice(order, newTP))
                .retryWhen(positionRetryLogic::shouldRetry)
                .doOnNext(orderEvent -> logger.debug("Changed TP from " + currentTP + " to " + newTP +
                        " for order " + orderToChangeTP.getLabel() + " and position "
                        + orderToChangeTP.getInstrument()))
                .toCompletable();
    }
}
