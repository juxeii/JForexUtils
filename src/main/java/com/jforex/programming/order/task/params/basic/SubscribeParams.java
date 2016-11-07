package com.jforex.programming.order.task.params.basic;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.RetryParams;

import io.reactivex.functions.Action;

public class SubscribeParams implements RetryParams {

    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;
    private final Consumer<Throwable> errorConsumer;
    private final Action startAction;
    private final Action completeAction;
    private final int noOfRetries;
    private final long delayInMillis;

    public SubscribeParams(final BasicParamsBuilder<?> paramsBuilderBase) {
        consumerForEvent = paramsBuilderBase.consumerForEvent;
        errorConsumer = paramsBuilderBase.errorConsumer;
        startAction = paramsBuilderBase.startAction;
        completeAction = paramsBuilderBase.completeAction;
        noOfRetries = paramsBuilderBase.noOfRetries;
        delayInMillis = paramsBuilderBase.delayInMillis;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    public Consumer<Throwable> errorConsumer() {
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
