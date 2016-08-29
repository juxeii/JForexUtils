package com.jforex.programming.order.builder;

import java.util.function.Consumer;

public abstract class OrderBuilder {

    protected Consumer<Throwable> errorAction;
    protected int noOfRetries;
    protected long delayInMillis;

    protected OrderBuilder(final CommonBuilder<?> builder) {
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
    }

    public final Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public final int noOfRetries() {
        return noOfRetries;
    }

    public final long delayInMillis() {
        return delayInMillis;
    }
}
