package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BasicTask;

public class SubmitParams extends BasicTaskParams {

    private final OrderParams orderParams;

    private SubmitParams(final Builder builder) {
        super(builder);

        this.orderParams = builder.orderParams;
    }

    public void subscribe(final BasicTask basicTask) {
        subscribe(basicTask.submitOrder(orderParams));
    }

    public static Builder withOrderParams(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return new Builder(orderParams);
    }

    public static class Builder extends ParamsBuilderBase<Builder> {

        private final OrderParams orderParams;

        public Builder(final OrderParams orderParams) {
            this.orderParams = orderParams;
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

        public SubmitParams build() {
            return new SubmitParams(this);
        }
    }
}
