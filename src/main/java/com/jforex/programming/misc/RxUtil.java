package com.jforex.programming.misc;

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

public final class RxUtil {

    private static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private static final long delayOnOrderFailRetry = platformSettings.delayOnOrderFailRetry();
    private static final int maxRetriesOnOrderFail = platformSettings.maxRetriesOnOrderFail();
    private static final Logger logger = LogManager.getLogger(RxUtil.class);

    private RxUtil() {
    }

    public final static <T> Observable<T> connectObservable(final Observable<T> observable) {
        final ConnectableObservable<T> connectableObservable = observable.replay();
        connectableObservable.connect();
        return connectableObservable;
    }

    public final static Completable connectCompletable(final Completable completable) {
        final Observable<OrderEvent> connectedObservable = connectObservable(completable.toObservable());
        return connectedObservable.toCompletable();
    }

    public final static Observable<Long> retryWithDelay(final Observable<? extends Throwable> errors,
                                                        final long delay,
                                                        final TimeUnit timeUnit) {
        return errors.flatMap(retryPair -> retryWait(delay, timeUnit));
    }

    public final static Observable<Long> retryWithDelay(final Observable<? extends Throwable> errors,
                                                        final long delay,
                                                        final TimeUnit timeUnit,
                                                        final int maxRetries) {
        return errors
                .zipWith(retryCounter(maxRetries), Pair::of)
                .flatMap(retryPair -> evaluateRetryPair(retryPair,
                                                        delay,
                                                        timeUnit,
                                                        maxRetries));
    }

    public final static Observable<Long> positionTaskRetry(final Observable<? extends Throwable> errors) {
        return errors
                .flatMap(RxUtil::filterErrorType)
                .zipWith(retryCounter(maxRetriesOnOrderFail), Pair::of)
                .flatMap(retryPair -> evaluateRetryPair(retryPair,
                                                        delayOnOrderFailRetry,
                                                        TimeUnit.MILLISECONDS,
                                                        maxRetriesOnOrderFail));
    }

    public final static Observable<Integer> retryCounter(final int maxRetries) {
        return Observable.range(1, maxRetries + 1);
    }

    private final static Observable<Long> evaluateRetryPair(final Pair<? extends Throwable, Integer> retryPair,
                                                            final long delay,
                                                            final TimeUnit timeUnit,
                                                            final int maxRetries) {
        return retryPair.getRight() == maxRetries + 1
                ? Observable.error(retryPair.getLeft())
                : retryWait(delay, timeUnit);
    }

    public static Observable<Long> retryWait(final long delay,
                                             final TimeUnit timeUnit) {
        return Observable.interval(delay, timeUnit).take(1);
    }

    private final static Observable<? extends Throwable> filterErrorType(final Throwable error) {
        if (error instanceof OrderCallRejectException) {
            logRetry((OrderCallRejectException) error);
            return Observable.just(error);
        }
        logger.error("Retry logic received unexpected error " + error.getClass().getName() + "!");
        return Observable.error(error);
    }

    private final static void logRetry(final OrderCallRejectException rejectException) {
        logger.warn("Received reject type " + rejectException.orderEvent().type() +
                " for order " + rejectException.orderEvent().order().getLabel() + "!"
                + " Will retry task in " + delayOnOrderFailRetry + " milliseconds...");
    }
}
