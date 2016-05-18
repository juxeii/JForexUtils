package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.settings.PlatformSettings;

import rx.Completable;
import rx.Observable;

public class PositionTask {

    private final OrderUtil orderUtil;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private final static int exceededRetryCount = platformSettings.maxRetriesOnOrderFail() + 1;
    private static final Logger logger = LogManager.getLogger(Position.class);

    public PositionTask(final OrderUtil orderUtil) {
        this.orderUtil = orderUtil;
    }

    public Completable closeCompletable(final IOrder orderToClose) {
        return Observable.just(orderToClose)
                .filter(order -> !isClosed.test(order))
                .doOnNext(order -> logger.debug("Starting to close order " + orderToClose.getLabel()
                        + " for " + orderToClose.getInstrument() + " position."))
                .flatMap(order -> orderUtil.close(order))
                .retryWhen(this::shouldRetry)
                .doOnNext(orderEvent -> logger.debug("Order " + orderToClose.getLabel() + " closed for "
                        + orderToClose.getInstrument() + " position."))
                .toCompletable();
    }

    public Observable<IOrder> mergeObservable(final String mergeLabel,
                                              final Set<IOrder> ordersToMerge) {
        logger.debug("Start merge with label " + mergeLabel);
        return Observable.defer(() -> orderUtil.mergeOrders(mergeLabel, ordersToMerge))
                .retryWhen(this::shouldRetry)
                .flatMap(orderEvent -> Observable.just(orderEvent.order()));
    }

    public Completable setSLCompletable(final IOrder orderToChangeSL,
                                        final double newSL) {
        final double currentSL = orderToChangeSL.getStopLossPrice();
        return Observable.just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(orderToChangeSL))
                .doOnNext(order -> logger.debug("Start to change SL from " + currentSL + " to "
                        + newSL + " for order " + order.getLabel() + " and position "
                        + orderToChangeSL.getInstrument()))
                .flatMap(order -> orderUtil.setStopLossPrice(order, newSL))
                .retryWhen(this::shouldRetry)
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
                .flatMap(order -> orderUtil.setTakeProfitPrice(order, newTP))
                .retryWhen(this::shouldRetry)
                .doOnNext(orderEvent -> logger.debug("Changed TP from " + currentTP + " to " + newTP +
                        " for order " + orderToChangeTP.getLabel() + " and position "
                        + orderToChangeTP.getInstrument()))
                .toCompletable();
    }

    private Observable<?> shouldRetry(final Observable<? extends Throwable> errors) {
        return errors
                .flatMap(this::filterErrorType)
                .zipWith(Observable.range(1, exceededRetryCount), Pair::of)
                .flatMap(this::evaluateRetryPair);
    }

    private Observable<Long> evaluateRetryPair(final Pair<? extends Throwable, Integer> retryPair) {
        return retryPair.getRight() == exceededRetryCount
                ? Observable.error(retryPair.getLeft())
                : Observable
                        .interval(platformSettings.delayOnOrderFailRetry(), TimeUnit.MILLISECONDS)
                        .take(1);
    }

    private Observable<? extends Throwable> filterErrorType(final Throwable error) {
        if (error instanceof OrderCallRejectException) {
            logRetry((OrderCallRejectException) error);
            return Observable.just(error);
        }
        logger.error("Retry logic received unexpected error " + error.getClass().getName() + "!");
        return Observable.error(error);
    }

    private void logRetry(final OrderCallRejectException rejectException) {
        logger.warn("Received reject type " + rejectException.orderEvent().type() +
                " for order " + rejectException.orderEvent().order().getLabel() + "!"
                + " Will retry task in " + platformSettings.delayOnOrderFailRetry() + " milliseconds...");
    }
}
