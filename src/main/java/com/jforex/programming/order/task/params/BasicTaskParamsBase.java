package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public abstract class BasicTaskParamsBase implements TaskParamsBase {

    protected TaskComposeAndEventMapData taskComposeAndEventMapData;

    protected BasicTaskParamsBase(final BasicParamsBuilder<?> builder) {
        taskComposeAndEventMapData = new TaskComposeAndEventMapData(builder);
    }

    @Override
    public abstract TaskParamsType type();

    @Override
    public ComposeData composeData() {
        return taskComposeAndEventMapData.composeData();
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return taskComposeAndEventMapData.consumerForEvent();
    }
}
