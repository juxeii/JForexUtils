package com.jforex.programming.order.call;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JFCallable;
import com.jforex.programming.misc.JForexUtil;

import rx.Observable;

public class OrderCallExecutor {

    private final IContext context;

    public OrderCallExecutor(final IContext context) {
        this.context = context;
    }

    public Observable<IOrder> callObservable(final JFCallable<IOrder> orderCallable) {
        return JForexUtil.isStrategyThread()
                ? Observable.fromCallable(orderCallable)
                : Observable.defer(() -> futureObservable(orderCallable));
    }

    private final Observable<IOrder> futureObservable(final JFCallable<IOrder> orderCallable) {
        return Observable.from(context.executeTask(orderCallable));
    }
}
