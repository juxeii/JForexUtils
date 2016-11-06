package com.jforex.programming.order.task.params;

public abstract class BasicTaskParamsBase {

    private final SubscribeParams subscribeParams;

    protected BasicTaskParamsBase(final GeneralBuilder<?> builder) {
        subscribeParams = new SubscribeParams(builder);
    }

    public SubscribeParams subscribeParams() {
        return subscribeParams;
    }
}
