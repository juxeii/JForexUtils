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
        return execute(() -> {
            action.run();
            return 1L;
        }).toCompletable();
    }

    public <T> Single<T> execute(final Callable<T> callable) {
        return JForexUtil.isStrategyThread()
                ? Single.fromCallable(callable)
                : Single.defer(() -> Single.fromFuture(context.executeTask(callable)));
    }
}
