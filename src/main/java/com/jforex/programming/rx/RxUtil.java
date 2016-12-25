package com.jforex.programming.rx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Action;

public final class RxUtil {

    private RxUtil() {
    }

    public static final RetryWhenFunction retryWhen(final int noOfRetries,
                                                    final RetryDelayFunction delayFunction) {
        return failures -> failures
            .zipWith(retryCounter(noOfRetries), (err, attempt) -> {
                if (attempt <= noOfRetries) {
                    final RetryDelay retryDelay = delayFunction.apply(attempt);
                    return wait(retryDelay.delay(), retryDelay.timeUnit());
                }
                return Observable.<Long> error(err);
            })
            .flatMap(x -> x);
    }

    public static final ObservableTransformer<Throwable, Long>
           retryWhenComposer(final int noOfRetries,
                             final RetryDelayFunction delayFunction) {
        return failures -> failures
            .zipWith(retryCounter(noOfRetries), (err, attempt) -> {
                if (attempt <= noOfRetries) {
                    final RetryDelay retryDelay = delayFunction.apply(attempt);
                    return wait(retryDelay.delay(), retryDelay.timeUnit());
                }
                return Observable.<Long> error(err);
            })
            .flatMap(x -> x);
    }

    public static final Observable<Integer> retryCounter(final int noOfRetries) {
        return Observable.range(1, noOfRetries + 1);
    }

    public static final Observable<Long> wait(final long delay,
                                              final TimeUnit timeUnit) {
        return Observable.timer(delay, timeUnit);
    }

    public static final Callable<Boolean> actionToCallable(final Action action) {
        return () -> {
            action.run();
            return true;
        };
    }
}
