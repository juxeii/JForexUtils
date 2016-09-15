package com.jforex.programming.misc;

import java.util.concurrent.Callable;

import com.dukascopy.api.IContext;

import io.reactivex.Single;

public class TaskExecutor {

    private final IContext context;

    public TaskExecutor(final IContext context) {
        this.context = context;
    }

    public <T> Single<T> onStrategyThread(final Callable<T> callable) {
        return JForexUtil.isStrategyThread()
                ? onCurrentThread(callable)
                : Single.defer(() -> Single.fromFuture(context.executeTask(callable)));
    }

    public <T> Single<T> onCurrentThread(final Callable<T> callable) {
        return Single.fromCallable(callable);
    }
}
