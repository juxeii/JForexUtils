package com.jforex.programming.order.task.params.position;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;

public class SimpleClosePositionParams extends PositionParamsBase {

    private SimpleClosePositionParams(final Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        public Builder doOnClose(final Consumer<OrderEvent> closeConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_OK, closeConsumer);
        }

        public Builder doOnPartialClose(final Consumer<OrderEvent> partialCloseConsumer) {
            return setEventConsumer(OrderEventType.PARTIAL_CLOSE_OK, partialCloseConsumer);
        }

        public Builder doOnReject(final Consumer<OrderEvent> rejectConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_REJECTED, rejectConsumer);
        }

        public SimpleClosePositionParams build() {
            return new SimpleClosePositionParams(this);
        }
    }
}
