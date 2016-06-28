package com.jforex.programming.order.call;

import java.util.concurrent.Callable;

import com.jforex.programming.misc.JForexUtil;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IOrder;

import rx.Observable;

public class OrderCallExecutor {

    private final IContext context;

    public OrderCallExecutor(final IContext context) {
        this.context = context;
    }

    public Observable<IOrder> callObservable(final Callable<IOrder> orderCallable) {
        return JForexUtil.isStrategyThread()
                ? Observable.fromCallable(orderCallable)
                : Observable.defer(() -> contextCallObservable(orderCallable));
    }

    private final Observable<IOrder> contextCallObservable(final Callable<IOrder> orderCallable) {
        return Observable.from(context.executeTask(orderCallable));
    }
}
