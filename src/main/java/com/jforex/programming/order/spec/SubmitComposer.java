package com.jforex.programming.order.spec;

import com.jforex.programming.order.event.OrderEventType;

public class SubmitComposer extends ObservableComposer {

    protected SubmitComposer(final SubmitBuilder builder) {
        super(builder);
    }

    public static class SubmitBuilder extends ObservableComposerBuilder<SubmitBuilder> {

        public SubmitBuilder doOnSubmit(final OrderEventConsumer submitConsumer) {
            return setEventConsumer(OrderEventType.SUBMIT_OK, submitConsumer);
        }

        public SubmitBuilder doOnPartialFill(final OrderEventConsumer partialFillConsumer) {
            return setEventConsumer(OrderEventType.PARTIAL_FILL_OK, partialFillConsumer);
        }

        public SubmitBuilder doOnFullFill(final OrderEventConsumer fullFillConsumer) {
            return setEventConsumer(OrderEventType.FULLY_FILLED, fullFillConsumer);
        }

        public SubmitBuilder doOnSubmitReject(final OrderEventConsumer submitRejectConsumer) {
            return setEventConsumer(OrderEventType.SUBMIT_REJECTED, submitRejectConsumer);
        }

        public SubmitBuilder doOnFillReject(final OrderEventConsumer fillRejectConsumer) {
            return setEventConsumer(OrderEventType.FILL_REJECTED, fillRejectConsumer);
        }

        public SubmitComposer build() {
            return new SubmitComposer(this);
        }
    }
}
