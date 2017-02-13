package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.functions.Action;

public interface ComposeData {

    public Action startAction();

    public Action completeAction();

    public Consumer<Throwable> errorConsumer();

    public RetryParams retryParams();

    public Map<OrderEventType, Consumer<OrderEvent>> consumerByEventType();
}
