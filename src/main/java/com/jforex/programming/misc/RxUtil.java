package com.jforex.programming.misc;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public final class RxUtil {

    private RxUtil() {
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

    public static final ObservableTransformer<Throwable, Long> retryComposer(final int noOfRetries,
                                                                             final long delay,
                                                                             final TimeUnit timeUnit) {
        return errors -> errors
            .zipWith(counterObservable(noOfRetries), Pair::of)
            .flatMap(retryPair -> retryPair.getRight() > noOfRetries
                    ? Observable.error(retryPair.getLeft())
                    : waitObservable(delay, timeUnit));
    }
}
