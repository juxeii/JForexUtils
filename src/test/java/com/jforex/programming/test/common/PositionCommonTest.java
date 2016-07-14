package com.jforex.programming.test.common;

import java.util.function.Supplier;

import com.dukascopy.api.JFException;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;
import rx.observers.TestSubscriber;

public class PositionCommonTest extends InstrumentUtilForTest {

    public static int retryExceedCount = platformSettings.maxRetriesOnOrderFail() + 1;
    public static final double noSL = platformSettings.noSLPrice();
    public static final double noTP = platformSettings.noTPPrice();

    public Observable<OrderEvent>[] retryRejectErrorObservableArray(final OrderEvent orderEvent,
                                                                    final int times) {
        @SuppressWarnings("unchecked")
        final Observable<OrderEvent>[] retryRejectErrorObservableArray = new Observable[times];
        for (int i = 0; i < times; ++i)
            retryRejectErrorObservableArray[i] = rejectObservable(orderEvent);
        return retryRejectErrorObservableArray;
    }

    public Observable<OrderEvent>[] rejectObservablesForFullRetries(final OrderEvent orderEvent) {
        return retryRejectErrorObservableArray(orderEvent,
                                               platformSettings.maxRetriesOnOrderFail());
    }

    public OrderCallRejectException createRejectException(final OrderEvent orderEvent) {
        return new OrderCallRejectException("", orderEvent);
    }

    public Observable<OrderEvent> exceptionObservable() {
        return Observable.error(jfException);
    }

    public Observable<OrderEvent> doneObservable() {
        return Observable.empty();
    }

    public Observable<OrderEvent> doneEventObservable(final OrderEvent orderEvent) {
        return Observable.just(orderEvent);
    }

    public Observable<OrderEvent> busyObservable() {
        return Observable.never();
    }

    public Observable<OrderEvent> rejectObservable(final OrderEvent orderEvent) {
        return Observable.error(createRejectException(orderEvent));
    }

    public void setRetryExceededMock(final Supplier<Observable<OrderEvent>> call,
                                     final OrderEvent orderEvent) {
        when(call.get()).thenReturn(rejectObservable(orderEvent),
                                    rejectObservablesForFullRetries(orderEvent));
    }

    public void setFullRetryMock(final Supplier<Observable<OrderEvent>> call,
                                 final OrderEvent orderEvent) {
        final Observable<OrderEvent>[] retryObservables =
                retryRejectErrorObservableArray(orderEvent,
                                                platformSettings.maxRetriesOnOrderFail() - 1);
        when(call.get())
                .thenReturn(rejectObservable(orderEvent),
                            retryObservables)
                .thenReturn(Observable.empty());
    }

    public void assertJFException(final TestSubscriber<?> subscriber) {
        subscriber.assertValueCount(0);
        subscriber.assertError(JFException.class);
    }

    public void assertRejectException(final TestSubscriber<?> subscriber) {
        subscriber.assertValueCount(0);
        subscriber.assertError(OrderCallRejectException.class);
    }
}
