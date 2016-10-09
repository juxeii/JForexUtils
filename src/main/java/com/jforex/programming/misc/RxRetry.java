package com.jforex.programming.misc;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;

import io.reactivex.Observable;

public final class RxRetry {

    private RxRetry() {
    }

    public static final Observable<Integer> counterObservable(final int maxRetries) {
        return Observable.range(1, maxRetries + 1);
    }

    public static final Observable<Long> waitObservable(final long delay,
                                                        final TimeUnit timeUnit) {
        return Observable
            .interval(delay, timeUnit)
            .take(1);
    }

    public static final Observable<Long> checkRetriesObservable(final Pair<? extends Throwable, Integer> retryPair,
                                                                final long delay,
                                                                final TimeUnit timeUnit,
                                                                final int maxRetries) {
        return retryPair.getRight() > maxRetries
                ? Observable.error(retryPair.getLeft())
                : waitObservable(delay, timeUnit);
    }
}
