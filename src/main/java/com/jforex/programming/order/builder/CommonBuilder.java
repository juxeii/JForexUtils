package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class CommonBuilder<T extends CommonBuilder<T>> {

    protected Consumer<Throwable> errorAction = o -> {};
    protected int noOfRetries;
    protected long delayInMillis;

    public T onError(final Consumer<Throwable> errorAction) {
        this.errorAction = checkNotNull(errorAction);
        return (T) this;
    }

    public T doRetries(final int noOfRetries,
                       final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
        return (T) this;
    }
}
