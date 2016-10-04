package com.jforex.programming.misc;

import java.util.concurrent.Callable;

import com.dukascopy.api.IContext;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;

public class StrategyThreadTask {

    private final IContext context;

    public StrategyThreadTask(final IContext context) {
        this.context = context;
    }

    public Completable execute(final Action action) {
        return JForexUtil.isStrategyThread()
                ? Completable.fromAction(action)
                : Completable.defer(() -> Completable.fromFuture(context.executeTask(actionToCallable(action))));
    }

    public <T> Single<T> execute(final Callable<T> callable) {
        return JForexUtil.isStrategyThread()
                ? Single.fromCallable(callable)
                : Single.defer(() -> Single.fromFuture(context.executeTask(callable)));
    }

    private final Callable<Void> actionToCallable(final Action action) {
        return () -> {
            action.run();
            return null;
        };
    }
}
