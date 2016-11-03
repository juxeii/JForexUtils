package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class SubmitSpec extends SpecBase<SubmitSpec.Builder, SubmitSpec> {

    private SubmitSpec(final Builder builder) {
        super(builder);
    }

    public static Builder submitSpec(final Observable<OrderEvent> observable) {
        return new Builder(observable);
    }

    public static class Builder extends SpecBuilderBase<Builder, SubmitSpec> {

        public Builder(final Observable<OrderEvent> observable) {
            super(observable);
        }

        public Builder doOnSubmit(final OrderEventConsumer submitConsumer) {
            return setEventConsumer(OrderEventType.SUBMIT_OK, submitConsumer);
        }

        public Builder doOnPartialFill(final OrderEventConsumer partialFillConsumer) {
            return setEventConsumer(OrderEventType.PARTIAL_FILL_OK, partialFillConsumer);
        }

        public Builder doOnFullFill(final OrderEventConsumer fullFillConsumer) {
            return setEventConsumer(OrderEventType.FULLY_FILLED, fullFillConsumer);
        }

        public Builder doOnSubmitReject(final OrderEventConsumer submitRejectConsumer) {
            return setEventConsumer(OrderEventType.SUBMIT_REJECTED, submitRejectConsumer);
        }

        public Builder doOnFillReject(final OrderEventConsumer fillRejectConsumer) {
            return setEventConsumer(OrderEventType.FILL_REJECTED, fillRejectConsumer);
        }

        @Override
        public SubmitSpec start() {
            return new SubmitSpec(this);
        }
    }
}
