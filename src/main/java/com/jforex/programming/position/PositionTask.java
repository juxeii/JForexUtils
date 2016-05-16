package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.settings.PlatformSettings;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Completable;
import rx.Observable;

public class PositionTask {

    private final Instrument instrument;
    private final OrderUtil orderUtil;
    private final ConcurrentUtil concurrentUtil;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private static final Logger logger = LogManager.getLogger(Position.class);

    public PositionTask(final Instrument instrument,
                        final OrderUtil orderUtil,
                        final ConcurrentUtil concurrentUtil) {
        this.instrument = instrument;
        this.orderUtil = orderUtil;
        this.concurrentUtil = concurrentUtil;
    }

    public Completable setSLCompletable(final IOrder orderToChangeSL,
                                        final double newSL) {
        final double currentSL = orderToChangeSL.getStopLossPrice();
        return Observable.just(orderToChangeSL)
                .filter(order -> !isSLSetTo(newSL).test(orderToChangeSL))
                .doOnNext(order -> logger.debug("Start to change SL from " + currentSL + " to "
                        + newSL + " for order " + order.getLabel() + " and position " + instrument))
                .flatMap(order -> orderUtil.setStopLossPrice(order, newSL))
                .retryWhen(this::shouldRetry)
                .doOnCompleted(() -> logger
                        .debug("Changed SL from " + currentSL + " to " + newSL +
                                " for order " + orderToChangeSL.getLabel() + " and position " + instrument))
                .toCompletable();
    }

    private Observable<?> shouldRetry(final Observable<? extends Throwable> errors) {
        return errors
                .flatMap(this::filterRetryError)
                .zipWith(Observable.range(1, platformSettings.maxRetriesOnOrderFail() + 1), Pair::of)
                .flatMap(this::evaluateRetryPair);
    }

    private Observable<Long> evaluateRetryPair(final Pair<? extends Throwable, Integer> retryPair) {
        return retryPair.getRight() == platformSettings.maxRetriesOnOrderFail() + 1
                ? Observable.error(retryPair.getLeft())
                : concurrentUtil.timerObservable(platformSettings.delayOnOrderFailRetry(),
                                                 TimeUnit.MILLISECONDS);
    }

    private Observable<? extends Throwable> filterRetryError(final Throwable error) {
        if (error instanceof OrderCallRejectException) {
            logRetry((OrderCallRejectException) error);
            return Observable.just(error);
        }
        logger.error("Retry logic received unexpected error " + error.getClass().getName() + "!");
        return Observable.error(error);
    }

    private void logRetry(final OrderCallRejectException rejectException) {
        logger.warn("Received reject type " + rejectException.orderEvent().type() +
                " for order " + rejectException.orderEvent().order().getLabel()
                + "!" + " Will retry task in " + platformSettings.delayOnOrderFailRetry() + " milliseconds...");
    }
}
