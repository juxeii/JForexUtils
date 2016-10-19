package com.jforex.programming.misc;

import java.util.concurrent.Callable;

import com.dukascopy.api.IContext;
import com.jforex.programming.init.JForexUtil;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;

public class StrategyThreadRunner {

    private final IContext context;

    public StrategyThreadRunner(final IContext context) {
        this.context = context;
    }

    public Completable execute(final Action action) {
        return execute(RxUtil.actionToCallable(action)).toCompletable();
    }

    public <T> Single<T> execute(final Callable<T> callable) {
        return JForexUtil.isStrategyThread()
                ? Single.fromCallable(callable)
                : Single.defer(() -> Single.fromFuture(context.executeTask(callable)));
    }
}
