package com.jforex.programming.order.task.params.position;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class PositionParams {

    protected ComposeData composeData;
    protected Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    private PositionParams(final Builder builder) {
        composeData = builder.composeParams;
        consumerForEvent = builder.consumerForEvent;
    }

    public ComposeData composeData() {
        return composeData;
    }

    public Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent() {
        return consumerForEvent;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        public PositionParams build() {
            return new PositionParams(this);
        }
    }
}
