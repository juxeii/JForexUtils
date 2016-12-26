package com.jforex.programming.order.task;

import static com.jforex.programming.order.event.OrderEventTypeSets.rejectEvents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.rx.RetryPredicate;
import com.jforex.programming.rx.RxUtil;

import io.reactivex.Observable;

public class TaskRetry {

    private static final Logger logger = LogManager.getLogger(TaskRetry.class);

    private TaskRetry() {
    }

    public static Observable<OrderEvent> rejectObservable(final Observable<OrderEvent> observable,
                                                          final RetryParams retryParams) {
        return observable
            .flatMap(TaskRetry::rejectAsError)
            .retryWhen(RxUtil.retryWithDelay(retryParams, retryPredicate(retryParams)));
    }

    private final static Observable<OrderEvent> rejectAsError(final OrderEvent orderEvent) {
        return rejectEvents.contains(orderEvent.type())
                ? Observable.error(new OrderCallRejectException("Reject event", orderEvent))
                : Observable.just(orderEvent);
    }

    private static final RetryPredicate retryPredicate(final RetryParams retryParams) {
        return (err, attempt) -> attempt <= retryParams.noOfRetries() && isRejectError(err);
    }

    private static final boolean isRejectError(final Throwable error) {
        if (error instanceof OrderCallRejectException) {
            logPositionTaskRetry((OrderCallRejectException) error);
            return true;
        }
        return false;
    }

    private static final void logPositionTaskRetry(final OrderCallRejectException rejectException) {
        logger.warn("Received reject type " + rejectException.orderEvent().type() +
                " for order " + rejectException.orderEvent().order().getLabel() + "!"
                + " Will retry now...");
    }
}
