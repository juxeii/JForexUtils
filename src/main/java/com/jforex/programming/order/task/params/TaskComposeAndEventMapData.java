package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class TaskComposeAndEventMapData {

    private final ComposeData composeData;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    public TaskComposeAndEventMapData(final BasicParamsBuilder<?> builder) {
        this.composeData = builder.composeParams;
        this.consumerForEvent = builder.consumerForEvent;
    }

    public ComposeData composeData() {
        return composeData;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }
}
