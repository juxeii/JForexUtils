package com.jforex.programming.misc;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

public final class RxUtil {

    private RxUtil() {
    }

    public static final Function<? super Observable<Throwable>,
                                 ? extends ObservableSource<?>>
           retryWhen(final int noOfRetries,
                     final long delay,
                     final TimeUnit timeUnit) {
        return errors -> errors
            .zipWith(retryCounter(noOfRetries), Pair::of)
            .flatMap(retryPair -> retryPair.getRight() > noOfRetries
                    ? Observable.error(retryPair.getLeft())
                    : wait(delay, timeUnit));
    }

    public static final ObservableTransformer<Throwable, Long> retryComposer(final int noOfRetries,
                                                                             final long delay,
                                                                             final TimeUnit timeUnit) {
        return errors -> errors
            .zipWith(retryCounter(noOfRetries), Pair::of)
            .flatMap(retryPair -> retryPair.getRight() > noOfRetries
                    ? Observable.error(retryPair.getLeft())
                    : wait(delay, timeUnit));
    }

    public static final Observable<Integer> retryCounter(final int maxRetries) {
        return Observable.range(1, maxRetries + 1);
    }

    public static final Observable<Long> wait(final long delay,
                                              final TimeUnit timeUnit) {
        return Observable
            .interval(delay, timeUnit)
            .take(1);
    }

    public static final Callable<Boolean> actionToCallable(final Action action) {
        return () -> {
            action.run();
            return true;
        };
    }
}
