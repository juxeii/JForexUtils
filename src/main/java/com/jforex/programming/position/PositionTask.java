package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
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
        logger.debug("Called changeSLOrderObs for " + orderToChangeSL.getLabel() + " with new SL " + newSL);

        return Completable.create(subscriber -> {
            Observable.just(orderToChangeSL)
                    .filter(order -> !isSLSetTo(newSL).test(orderToChangeSL))
                    .doOnNext(order -> logger.debug("Start to change SL from " + order.getStopLossPrice() + " to "
                            + newSL + " for order " + order.getLabel() + " and position " + instrument))
                    .flatMap(order -> orderUtil.setStopLossPrice(order, newSL))
                    .retry(this::shouldRetry)
                    .doOnCompleted(() -> logger.debug("Changed SL to " + orderToChangeSL.getStopLossPrice() +
                            " for order " + orderToChangeSL.getLabel() + " and position " + instrument))
                    .subscribe(orderEvent -> {},
                               subscriber::onError,
                               subscriber::onCompleted);
        });
    }

    private boolean shouldRetry(final int retryCount,
                                final Throwable throwable) {
        if (throwable instanceof OrderCallRejectException &&
                retryCount <= platformSettings.maxRetriesOnOrderFail()) {
            logRetry((OrderCallRejectException) throwable);
            concurrentUtil.timerObservable(platformSettings.delayOnOrderFailRetry(), TimeUnit.MILLISECONDS)
                    .toBlocking()
                    .subscribe(i -> {});
            return true;
        }
        return false;
    }

    private void logRetry(final OrderCallRejectException rejectException) {
        final IOrder order = rejectException.orderEvent().order();
        logger.warn("Received reject type " + rejectException.orderEvent().type() + " for order " + order.getLabel()
                + "!" + " Will retry task in " + platformSettings.delayOnOrderFailRetry() + " milliseconds...");
    }
}
