package com.jforex.programming.order.task.params;

import java.util.Map;

import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.functions.Action;

public class SubscribeParams implements RetryParams {

    private final Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    private final ErrorConsumer errorConsumer;
    private final Action startAction;
    private final Action completeAction;
    private final int noOfRetries;
    private final long delayInMillis;

    public SubscribeParams(final GeneralBuilder<?> paramsBuilderBase) {
        consumerForEvent = paramsBuilderBase.consumerForEvent;
        errorConsumer = paramsBuilderBase.errorConsumer;
        startAction = paramsBuilderBase.startAction;
        completeAction = paramsBuilderBase.completeAction;
        noOfRetries = paramsBuilderBase.noOfRetries;
        delayInMillis = paramsBuilderBase.delayInMillis;
    }

    public Map<OrderEventType, OrderEventConsumer> consumerForEvent() {
        return consumerForEvent;
    }

    public ErrorConsumer errorConsumer() {
        return errorConsumer;
    }

    public Action startAction() {
        return startAction;
    }

    public Action completeAction() {
        return completeAction;
    }

    @Override
    public int noOfRetries() {
        return noOfRetries;
    }

    @Override
    public long delayInMillis() {
        return delayInMillis;
    }
}
