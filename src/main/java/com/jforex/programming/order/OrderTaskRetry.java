package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventTypeSets.rejectEvents;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.StreamUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class OrderTaskRetry {

    private final int noOfRetries;
    private final long delayInMillis;

    private static final Logger logger = LogManager.getLogger(OrderTaskRetry.class);

    public OrderTaskRetry(final int noOfRetries,
                          final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
    }

    public static Function<Observable<OrderEvent>, Observable<OrderEvent>> onRejectRetryWith(final int noOfRetries,
                                                                                             final long delayInMillis) {
        final OrderTaskRetry orderTaskRetry = new OrderTaskRetry(noOfRetries, delayInMillis);
        return orderTaskRetry::retryTransform;
    }

    private final Observable<OrderEvent> retryTransform(final Observable<OrderEvent> sourceObservable) {
        return sourceObservable
            .flatMap(this::rejectAsError)
            .retryWhen(this::retryOnReject);
    }

    private final Observable<OrderEvent> rejectAsError(final OrderEvent orderEvent) {
        return rejectEvents.contains(orderEvent.type())
                ? Observable.error(new OrderCallRejectException("Reject event", orderEvent))
                : Observable.just(orderEvent);
    }

    private final Observable<Long> retryOnReject(final Observable<? extends Throwable> errors) {
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
