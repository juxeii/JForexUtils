package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.StreamUtil;
import com.jforex.programming.order.call.OrderCallRejectException;

import rx.Observable;

public class CommandRetry {

    private final int noOfRetries;
    private final long delayInMillis;

    private static final Logger logger = LogManager.getLogger(CommandRetry.class);

    public CommandRetry(final int noOfRetries,
                        final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
    }

    public final Observable<Long> retryOnRejectObservable(final Observable<? extends Throwable> errors) {
        return checkNotNull(errors)
            .flatMap(this::filterCallErrorType)
            .zipWith(StreamUtil.retryCounterObservable(noOfRetries), Pair::of)
            .flatMap(retryPair -> StreamUtil.evaluateRetryPair(retryPair,
                                                               delayInMillis,
                                                               TimeUnit.MILLISECONDS,
                                                               noOfRetries));
    }

    private final Observable<? extends Throwable> filterCallErrorType(final Throwable error) {
        if (error instanceof OrderCallRejectException) {
            logPositionTaskRetry((OrderCallRejectException) error);
            return Observable.just(error);
        }
        return Observable.error(error);
    }

    private final void logPositionTaskRetry(final OrderCallRejectException rejectException) {
        logger.warn("Received reject type " + rejectException.orderEvent().type() +
                " for order " + rejectException.orderEvent().order().getLabel() + "!"
                + " Will retry task in " + delayInMillis + " milliseconds...");
    }
}
