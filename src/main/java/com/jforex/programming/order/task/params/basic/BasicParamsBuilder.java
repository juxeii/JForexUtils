package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.jforex.programming.order.task.params.CommonParamsBuilder;

import io.reactivex.functions.Action;

@SuppressWarnings("unchecked")
public class BasicParamsBuilder<T> extends CommonParamsBuilder<T> {

    public Consumer<Throwable> errorConsumer;
    public Action startAction;
    public Action completeAction;

    public T doOnStart(final Action startAction) {
        checkNotNull(startAction);

        this.startAction = startAction;
        return (T) this;
    }

    public T doOnException(final Consumer<Throwable> errorConsumer) {
        checkNotNull(errorConsumer);

        this.errorConsumer = errorConsumer;
        return (T) this;
    }

    public T doOnComplete(final Action completeAction) {
        checkNotNull(completeAction);

        this.completeAction = completeAction;
        return (T) this;
    }
}
