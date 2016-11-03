package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class CloseSpec extends SpecBase<CloseSpec.Builder, CloseSpec> {

    private CloseSpec(final Builder builder) {
        super(builder);
    }

    public static Builder closeSpec(final Observable<OrderEvent> observable) {
        return new Builder(observable);
    }

    public static class Builder extends SpecBuilderBase<Builder, CloseSpec> {

        public Builder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public Builder doOnClose(final OrderEventConsumer closeConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_OK, closeConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer closeRejectConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_REJECTED, closeRejectConsumer);
        }

        @Override
        public CloseSpec start() {
            return new CloseSpec(this);
        }
    }
}
