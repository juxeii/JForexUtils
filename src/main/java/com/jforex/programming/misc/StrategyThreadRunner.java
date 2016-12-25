package com.jforex.programming.misc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Callable;

import com.dukascopy.api.IContext;
import com.jforex.programming.rx.RxUtil;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;

public class StrategyThreadRunner {

    private final IContext context;

    public StrategyThreadRunner(final IContext context) {
        this.context = context;
    }

    public Completable execute(final Action action) {
        checkNotNull(action);

        return execute(RxUtil.actionToCallable(action)).toCompletable();
    }

    public <T> Single<T> execute(final Callable<T> callable) {
        checkNotNull(callable);

        return StrategyUtil.isStrategyThread()
                ? Single.fromCallable(callable)
                : Single.defer(() -> Single.fromFuture(context.executeTask(callable)));
    }
}
