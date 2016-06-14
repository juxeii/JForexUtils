package com.jforex.programming.order.call;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.misc.JFCallable;

import rx.Observable;

public class OrderCallExecutor {

    private final ConcurrentUtil concurrentUtil;

    public OrderCallExecutor(final ConcurrentUtil concurrentUtil) {
        this.concurrentUtil = concurrentUtil;
    }

    public Observable<IOrder> callObservable(final JFCallable<IOrder> orderCallable) {
        return ConcurrentUtil.isStrategyThread()
                ? Observable.fromCallable(orderCallable)
                : Observable.defer(() -> futureObservable(orderCallable));
    }

    private final Observable<IOrder> futureObservable(final JFCallable<IOrder> orderCallable) {
        return Observable.from(concurrentUtil.executeOnStrategyThread(orderCallable));
    }
}
