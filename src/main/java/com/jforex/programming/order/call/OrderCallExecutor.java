package com.jforex.programming.order.call;

import java.util.concurrent.Callable;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JForexUtil;

import rx.Observable;

public class OrderCallExecutor {

    private final IContext context;

    public OrderCallExecutor(final IContext context) {
        this.context = context;
    }

    public Observable<IOrder> callObservable(final Callable<IOrder> orderCallable) {
        return JForexUtil.isStrategyThread()
                ? Observable.fromCallable(orderCallable)
                : Observable.defer(() -> Observable.from(context.executeTask(orderCallable)));
    }
}
