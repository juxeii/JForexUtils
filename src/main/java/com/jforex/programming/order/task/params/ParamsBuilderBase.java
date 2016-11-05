package com.jforex.programming.order.task.params;

import java.util.Map;

import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.functions.Action;

public class ParamsBuilderBase {

    public Map<OrderEventType, OrderEventConsumer> consumerForEvent;
    public ErrorConsumer errorConsumer;
    public Action startAction;
    public Action completeAction;
    public int noOfRetries;
    public long delayInMillis;
}
