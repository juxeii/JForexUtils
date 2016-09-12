package com.jforex.programming.misc;

import java.util.concurrent.Callable;

import com.dukascopy.api.IContext;

import io.reactivex.Observable;

public class TaskExecutor {

    private final IContext context;

    public TaskExecutor(final IContext context) {
        this.context = context;
    }

    public <T> Observable<T> onStrategyThread(final Callable<T> callable) {
        return JForexUtil.isStrategyThread()
                ? onCurrentThread(callable)
                : Observable.defer(() -> Observable.fromFuture(context.executeTask(callable)));
    }

    public <T> Observable<T> onCurrentThread(final Callable<T> callable) {
        return Observable.fromCallable(callable);
    }
}
