package com.jforex.programming.order.task;

import static com.jforex.programming.order.event.OrderEventTypeSets.rejectEvents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.rx.RetryDelayFunction;
import com.jforex.programming.rx.RxUtil;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;

public class TaskRetry {

    private static final Logger logger = LogManager.getLogger(TaskRetry.class);

    private TaskRetry() {
    }

    public static ObservableTransformer<OrderEvent, OrderEvent> onRejectRetryWith(final int noOfRetries,
                                                                                  final RetryDelayFunction delayFunction) {
        return sourceObservable -> sourceObservable
            .flatMap(TaskRetry::rejectAsError)
            .retryWhen(retryOnReject(noOfRetries, delayFunction));
    }

    private final static Observable<OrderEvent> rejectAsError(final OrderEvent orderEvent) {
        return rejectEvents.contains(orderEvent.type())
                ? Observable.error(new OrderCallRejectException("Reject event", orderEvent))
                : Observable.just(orderEvent);
    }

    private static final Function<Observable<? extends Throwable>, Observable<Long>>
            retryOnReject(final int noOfRetries,
                          final RetryDelayFunction delayFunction) {
        return errors -> errors
            .flatMap(error -> filterCallErrorType(error, delayFunction))
            .compose(RxUtil.retryWhenComposer(noOfRetries, delayFunction));
    }

    private static final Observable<Throwable> filterCallErrorType(final Throwable error,
                                                                   final RetryDelayFunction delayFunction) {
        if (error instanceof OrderCallRejectException) {
            logPositionTaskRetry((OrderCallRejectException) error);
            return Observable.just(error);
        }
        return Observable.error(error);
    }

    private static final void logPositionTaskRetry(final OrderCallRejectException rejectException) {
        logger.warn("Received reject type " + rejectException.orderEvent().type() +
                " for order " + rejectException.orderEvent().order().getLabel() + "!"
                + " Will retry now...");
    }
}
