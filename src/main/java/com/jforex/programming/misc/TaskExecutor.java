package com.jforex.programming.misc;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.dukascopy.api.IContext;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;

public class TaskExecutor {

    private final IContext context;

    public TaskExecutor(final IContext context) {
        this.context = context;
    }

    public Completable onStrategyThread(final Action action) {
        return JForexUtil.isStrategyThread()
                ? onCurrentThread(action)
                : Completable.defer(() -> Completable.fromFuture(executeOnContext(action)));
    }

    public <T> Single<T> onStrategyThread(final Callable<T> callable) {
        return JForexUtil.isStrategyThread()
                ? onCurrentThread(callable)
                : Single.defer(() -> Single.fromFuture(executeOnContext(callable)));
    }

    public Completable onCurrentThread(final Action action) {
        return Completable.fromAction(action);
    }

    public <T> Single<T> onCurrentThread(final Callable<T> callable) {
        return Single.fromCallable(callable);
    }

    public Future<Void> executeOnContext(final Action action) {
        return context.executeTask(actionToCallable(action));
    }

    public <T> Future<T> executeOnContext(final Callable<T> callable) {
        return context.executeTask(callable);
    }

    private final Callable<Void> actionToCallable(final Action action) {
        return () -> {
            action.run();
            return null;
        };
    }
}
