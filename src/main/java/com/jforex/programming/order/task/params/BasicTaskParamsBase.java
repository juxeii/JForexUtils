package com.jforex.programming.order.task.params;

public abstract class BasicTaskParamsBase {

    private final SubscribeParams subscribeParams;

    protected BasicTaskParamsBase(final ParamsBuilderBase<?> builder) {
        subscribeParams = new SubscribeParams(builder);
    }

    public SubscribeParams subscribeParams() {
        return subscribeParams;
    }
}
