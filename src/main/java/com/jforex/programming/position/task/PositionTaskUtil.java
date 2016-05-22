package com.jforex.programming.position.task;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.settings.PlatformSettings;

import rx.Completable;
import rx.Observable;
import rx.observables.ConnectableObservable;

public class PositionTaskUtil {

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private final static int exceededRetryCount = platformSettings.maxRetriesOnOrderFail() + 1;
    private static final Logger logger = LogManager.getLogger(PositionTaskUtil.class);

    public static <T> Observable<T> connectObservable(final Observable<T> observable) {
        final ConnectableObservable<T> connectableObservable =
                observable.flatMap(orderEvent -> Observable.just(orderEvent))
                        .replay();

        connectableObservable.connect();
        return connectableObservable;
    }

    public static Completable connectCompletable(final Completable completable) {
        final Observable<OrderEvent> connectedObservable =
                connectObservable(completable.toObservable());

        return connectedObservable.toCompletable();
    }

    public static Observable<Long> shouldRetry(final Observable<? extends Throwable> errors) {
        return errors
                .flatMap(PositionTaskUtil::filterErrorType)
                .zipWith(Observable.range(1, exceededRetryCount), Pair::of)
                .flatMap(PositionTaskUtil::evaluateRetryPair);
    }

    private static Observable<Long> evaluateRetryPair(final Pair<? extends Throwable, Integer> retryPair) {
        return retryPair.getRight() == exceededRetryCount
                ? Observable.error(retryPair.getLeft())
                : Observable
                        .interval(platformSettings.delayOnOrderFailRetry(), TimeUnit.MILLISECONDS)
                        .take(1);
    }

    private static Observable<? extends Throwable> filterErrorType(final Throwable error) {
        if (error instanceof OrderCallRejectException) {
            logRetry((OrderCallRejectException) error);
            return Observable.just(error);
        }
        logger.error("Retry logic received unexpected error " + error.getClass().getName() + "!");
        return Observable.error(error);
    }

    private static void logRetry(final OrderCallRejectException rejectException) {
        logger.warn("Received reject type " + rejectException.orderEvent().type() +
                " for order " + rejectException.orderEvent().order().getLabel() + "!"
                + " Will retry task in " + platformSettings.delayOnOrderFailRetry() + " milliseconds...");
    }
}
