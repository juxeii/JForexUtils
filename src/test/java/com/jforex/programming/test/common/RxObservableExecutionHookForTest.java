package com.jforex.programming.test.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observable.Operator;
import rx.Subscription;
import rx.plugins.RxJavaObservableExecutionHook;

public class RxObservableExecutionHookForTest extends RxJavaObservableExecutionHook {

    private static final Logger logger = LogManager.getLogger(RxObservableExecutionHookForTest.class);

    @Override
    public <T> OnSubscribe<T> onCreate(final OnSubscribe<T> onSubscribe) {
        logger.debug("onCreate");
        return onSubscribe;
    }

    @Override
    public <T, R> Operator<? extends R, ? super T> onLift(final Operator<? extends R, ? super T> lift) {
        logger.debug("onLift");
        return lift;
    }

    @Override
    public <T> Throwable onSubscribeError(final Throwable throwable) {
        logger.debug("onSubscribeError");
        return throwable;
    }

    @Override
    public <T> Subscription onSubscribeReturn(final Subscription subscription) {
        logger.debug("onSubscribeReturn");
        return subscription;
    }

    @Override
    public <T> OnSubscribe<T> onSubscribeStart(final Observable<? extends T> observableInstance,
                                               final OnSubscribe<T> onSubscribe) {
        logger.debug("onSubscribeStart");
        return onSubscribe;
    }
}
