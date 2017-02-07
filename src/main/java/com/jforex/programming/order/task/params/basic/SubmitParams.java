package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.BasicTaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;

public class SubmitParams extends BasicTaskParamsBase {

    private final OrderParams orderParams;

    private SubmitParams(final Builder builder) {
        super(builder);

        this.orderParams = builder.orderParams;
    }

    public OrderParams orderParams() {
        return orderParams;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.SUBMIT;
    }

    public static Builder withOrderParams(final OrderParams orderParams) {
        checkNotNull(orderParams);

        return new Builder(orderParams);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final OrderParams orderParams;

        public Builder(final OrderParams orderParams) {
            this.orderParams = orderParams;
        }

        public Builder doOnSubmit(final Consumer<OrderEvent> submitConsumer) {
            return setEventConsumer(OrderEventType.SUBMIT_OK, submitConsumer);
        }

        public Builder doOnPartialFill(final Consumer<OrderEvent> partialFillConsumer) {
            return setEventConsumer(OrderEventType.PARTIAL_FILL_OK, partialFillConsumer);
        }

        public Builder doOnFullFill(final Consumer<OrderEvent> fullFillConsumer) {
            return setEventConsumer(OrderEventType.FULLY_FILLED, fullFillConsumer);
        }

        public Builder doOnSubmitReject(final Consumer<OrderEvent> submitRejectConsumer) {
            return setEventConsumer(OrderEventType.SUBMIT_REJECTED, submitRejectConsumer);
        }

        public Builder doOnFillReject(final Consumer<OrderEvent> fillRejectConsumer) {
            return setEventConsumer(OrderEventType.FILL_REJECTED, fillRejectConsumer);
        }

        public SubmitParams build() {
            return new SubmitParams(this);
        }
    }
}
