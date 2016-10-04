package com.jforex.programming.misc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import io.reactivex.Observable;

public final class StreamUtil {

    private StreamUtil() {
    }

    public static final Observable<Integer> retryCounterObservable(final int maxRetries) {
        return Observable.range(1, maxRetries + 1);
    }

    public static final Observable<Long> waitObservable(final long delay,
                                                        final TimeUnit timeUnit) {
        return Observable
            .interval(delay, timeUnit)
            .take(1);
    }

    public static final <T> Stream<T> optionalStream(final Optional<T> optional) {
        return checkNotNull(optional).isPresent()
                ? Stream.of(optional.get())
                : Stream.empty();
    }

    public static final Observable<Long> evaluateRetryPair(final Pair<? extends Throwable, Integer> retryPair,
                                                           final long delay,
                                                           final TimeUnit timeUnit,
                                                           final int maxRetries) {
        return retryPair.getRight() > maxRetries
                ? Observable.error(retryPair.getLeft())
                : waitObservable(delay, timeUnit);
    }
}
