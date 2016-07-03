package com.jforex.programming.misc;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.settings.PlatformSettings;

import rx.Completable;
import rx.Observable;

public final class StreamUtil {

    private static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private static final long delayOnOrderFailRetry = platformSettings.delayOnOrderFailRetry();
    private static final int maxRetriesOnOrderFail = platformSettings.maxRetriesOnOrderFail();
    private static final Logger logger = LogManager.getLogger(StreamUtil.class);

    private StreamUtil() {
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
                .zipWith(retryCounterObservable(maxRetries), Pair::of)
                .flatMap(retryPair -> evaluateRetryPair(retryPair,
                                                        delay,
                                                        timeUnit,
                                                        maxRetries));
    }

    public final static Observable<Long> positionTaskRetry(final Observable<? extends Throwable> errors) {
        return errors
                .flatMap(StreamUtil::filterErrorType)
                .zipWith(retryCounterObservable(maxRetriesOnOrderFail), Pair::of)
                .flatMap(retryPair -> evaluateRetryPair(retryPair,
                                                        delayOnOrderFailRetry,
                                                        TimeUnit.MILLISECONDS,
                                                        maxRetriesOnOrderFail));
    }

    public final static Observable<Integer> retryCounterObservable(final int maxRetries) {
        return Observable.range(1, maxRetries + 1);
    }

    public final static Completable CompletableFromJFRunnable(final JFRunnable jfRunnable) {
        return Completable.fromCallable(() -> {
            jfRunnable.run();
            return null;
        });
    }

    private final static Observable<Long> evaluateRetryPair(final Pair<? extends Throwable, Integer> retryPair,
                                                            final long delay,
                                                            final TimeUnit timeUnit,
                                                            final int maxRetries) {
        return retryPair.getRight() > maxRetries
                ? Observable.error(retryPair.getLeft())
                : retryWait(delay, timeUnit);
    }

    public static Observable<Long> retryWait(final long delay,
                                             final TimeUnit timeUnit) {
        return Observable
                .interval(delay, timeUnit)
                .take(1);
    }

    public static <T> Stream<T> streamOptional(final Optional<T> optional) {
        return optional.isPresent()
                ? Stream.of(optional.get())
                : Stream.empty();
    }

    private final static Observable<? extends Throwable> filterErrorType(final Throwable error) {
        if (error instanceof OrderCallRejectException) {
            logPositionTaskRetry((OrderCallRejectException) error);
            return Observable.just(error);
        }
        logger.error("Retry logic received unexpected error " + error.getClass().getName() + "!");
        return Observable.error(error);
    }

    private final static void logPositionTaskRetry(final OrderCallRejectException rejectException) {
        logger.warn("Received reject type " + rejectException.orderEvent().type() +
                " for order " + rejectException.orderEvent().order().getLabel() + "!"
                + " Will retry task in " + delayOnOrderFailRetry + " milliseconds...");
    }
}
