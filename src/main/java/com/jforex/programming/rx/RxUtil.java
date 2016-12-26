package com.jforex.programming.rx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.jforex.programming.order.task.params.RetryParams;

import io.reactivex.Observable;
import io.reactivex.functions.Action;

public final class RxUtil {

    private RxUtil() {
    }

    public static final RetryWhenFunction retryWithDelay(final RetryParams retryParams) {
        return retryWithDelay(retryParams,
                              (err, attempt) -> attempt <= retryParams.noOfRetries());
    }

    public static final RetryWhenFunction retryWithDelay(final RetryParams retryParams,
                                                         final RetryPredicate retryPredicate) {
        return failures -> failures
            .zipWith(retryCounter(retryParams.noOfRetries()), (err, attempt) -> retryPredicate.apply(err, attempt)
                    ? waitForRetry(retryParams, attempt)
                    : Observable.<Long> error(err))
            .flatMap(x -> x);
    }

    private static final Observable<Long> waitForRetry(final RetryParams retryParams,
                                                       final int attempt) {
        final RetryDelay retryDelay = retryParams
            .delayFunction()
            .apply(attempt);
        return wait(retryDelay.delay(), retryDelay.timeUnit());
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
