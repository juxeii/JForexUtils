package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public interface TaskParams {

    public TaskParamsType type();

    public ComposeData composeData();

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent();
}
