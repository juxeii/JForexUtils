package com.jforex.programming.order.task.params.basic;

public abstract class BasicParamsBase {

    private final SubscribeParams subscribeParams;

    protected BasicParamsBase(final BasicParamsBuilder<?> builder) {
        subscribeParams = new SubscribeParams(builder);
    }

    public SubscribeParams subscribeParams() {
        return subscribeParams;
    }
}
