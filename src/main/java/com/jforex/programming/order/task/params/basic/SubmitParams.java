package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class SubmitParams extends TaskParamsWithType {

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

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final OrderParams orderParams;

        public Builder(final OrderParams orderParams) {
            this.orderParams = orderParams;
        }

        public Builder doOnSubmit(final Consumer<OrderEvent> submitConsumer) {
            setEventConsumer(OrderEventType.SUBMIT_OK, submitConsumer);
            return this;
        }

        public Builder doOnPartialFill(final Consumer<OrderEvent> partialFillConsumer) {
            setEventConsumer(OrderEventType.PARTIAL_FILL_OK, partialFillConsumer);
            return this;
        }

        public Builder doOnFullFill(final Consumer<OrderEvent> fullFillConsumer) {
            setEventConsumer(OrderEventType.FULLY_FILLED, fullFillConsumer);
            return this;
        }

        public Builder doOnSubmitReject(final Consumer<OrderEvent> submitRejectConsumer) {
            setEventConsumer(OrderEventType.SUBMIT_REJECTED, submitRejectConsumer);
            return this;
        }

        public Builder doOnFillReject(final Consumer<OrderEvent> fillRejectConsumer) {
            setEventConsumer(OrderEventType.FILL_REJECTED, fillRejectConsumer);
            return this;
        }

        public SubmitParams build() {
            return new SubmitParams(this);
        }
    }
}
