package com.jforex.programming.misc;

import java.util.concurrent.Callable;

import com.dukascopy.api.IContext;

import io.reactivex.Flowable;

public class TaskExecutor {

    private final IContext context;

    public TaskExecutor(final IContext context) {
        this.context = context;
    }

    public <T> Flowable<T> onStrategyThread(final Callable<T> callable) {
        return JForexUtil.isStrategyThread()
                ? onCurrentThread(callable)
                : Flowable.defer(() -> Flowable.fromFuture(context.executeTask(callable)));
    }

    public <T> Flowable<T> onCurrentThread(final Callable<T> callable) {
        return Flowable.fromCallable(callable);
    }
}
