package com.jforex.programming.order.task.params.position;

import java.util.function.Consumer;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class ClosePositionParams extends PositionParamsBase<Instrument> {

    private ClosePositionParams(final Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends PositionParamsBuilder<Builder, Instrument> {

        public Builder doOnClose(final Consumer<OrderEvent> closeConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_OK, closeConsumer);
        }

        public Builder doOnPartialClose(final Consumer<OrderEvent> partialCloseConsumer) {
            return setEventConsumer(OrderEventType.PARTIAL_CLOSE_OK, partialCloseConsumer);
        }

        public Builder doOnReject(final Consumer<OrderEvent> rejectConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_REJECTED, rejectConsumer);
        }

        public ClosePositionParams build() {
            return new ClosePositionParams(this);
        }
    }
}
