package com.jforex.programming.misc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public final class StreamUtil {

    private StreamUtil() {
    }

    public static final Flowable<Integer> retryCounterFlowable(final int maxRetries) {
        return Flowable.range(1, maxRetries + 1);
    }

    public static final Flowable<Long> waitFlowable(final long delay,
                                                    final TimeUnit timeUnit) {
        return Flowable
            .interval(delay, timeUnit)
            .take(1);
    }

    public static final <T> Stream<T> optionalStream(final Optional<T> optional) {
        return checkNotNull(optional).isPresent()
                ? Stream.of(optional.get())
                : Stream.empty();
    }

    public static final Completable completableForJFRunnable(final JFRunnable jfRunnable) {
        checkNotNull(jfRunnable);

        return Completable.fromCallable(() -> {
            jfRunnable.run();
            return null;
        });
    }

    public static final Flowable<Long> evaluateRetryPair(final Pair<? extends Throwable, Integer> retryPair,
                                                         final long delay,
                                                         final TimeUnit timeUnit,
                                                         final int maxRetries) {
        return retryPair.getRight() > maxRetries
                ? Flowable.error(retryPair.getLeft())
                : waitFlowable(delay, timeUnit);
    }
}
