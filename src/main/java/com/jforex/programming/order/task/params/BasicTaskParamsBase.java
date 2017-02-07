package com.jforex.programming.order.task.params;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public abstract class BasicTaskParamsBase implements TaskParamsBase {

    protected ComposeData composeData;
    protected Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    protected BasicTaskParamsBase(final BasicParamsBuilder<?> builder) {
        composeData = builder.composeParams;
        consumerForEvent = builder.consumerForEvent;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    @Override
    public abstract TaskParamsType type();

    @Override
    public ComposeData composeData() {
        return composeData;
    }
}
