package com.jforex.programming.order.task;

import static com.jforex.programming.order.event.OrderEventTypeSets.rejectEvents;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.RxUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;

public class OrderTaskRetry {

    private static final Logger logger = LogManager.getLogger(OrderTaskRetry.class);

    private OrderTaskRetry() {
    }

    public static ObservableTransformer<OrderEvent, OrderEvent> onRejectRetryWith(final int noOfRetries,
                                                                                  final long delayInMillis) {
        return sourceObservable -> sourceObservable
            .flatMap(OrderTaskRetry::rejectAsError)
            .retryWhen(retryOnReject(noOfRetries, delayInMillis));
    }

    private final static Observable<OrderEvent> rejectAsError(final OrderEvent orderEvent) {
        return rejectEvents.contains(orderEvent.type())
                ? Observable.error(new OrderCallRejectException("Reject event", orderEvent))
                : Observable.just(orderEvent);
    }

    private static final Function<Observable<? extends Throwable>,
                                  Observable<Long>>
            retryOnReject(final int noOfRetries,
                          final long delayInMillis) {
        return errors -> errors
            .flatMap(error -> filterCallErrorType(error, delayInMillis))
            .compose(RxUtil.retryComposer(noOfRetries,
                                          delayInMillis,
                                          TimeUnit.MILLISECONDS));
    }

    private static final Observable<Throwable> filterCallErrorType(final Throwable error,
                                                                   final long delayInMillis) {
        if (error instanceof OrderCallRejectException) {
            logPositionTaskRetry((OrderCallRejectException) error, delayInMillis);
            return Observable.just(error);
        }
        return Observable.error(error);
    }

    private static final void logPositionTaskRetry(final OrderCallRejectException rejectException,
                                                   final long delayInMillis) {
        logger.warn("Received reject type " + rejectException.orderEvent().type() +
                " for order " + rejectException.orderEvent().order().getLabel() + "!"
                + " Will retry task in " + delayInMillis + " milliseconds...");
    }
}
