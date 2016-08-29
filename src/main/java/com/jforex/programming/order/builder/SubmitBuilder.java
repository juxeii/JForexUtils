package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;

public class SubmitBuilder {

    private final OrderParams orderParams;
    private final Consumer<Throwable> errorAction;
    private final Consumer<IOrder> submitRejectAction;
    private final Consumer<IOrder> fillRejectAction;
    private final Consumer<IOrder> submitOKAction;
    private final Consumer<IOrder> partialFillAction;
    private final Consumer<IOrder> fillAction;

    public final OrderParams orderParams() {
        return orderParams;
    }

    public final Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public final Consumer<IOrder> submitRejectAction() {
        return submitRejectAction;
    }

    public final Consumer<IOrder> fillRejectAction() {
        return fillRejectAction;
    }

    public final Consumer<IOrder> submitOKAction() {
        return submitOKAction;
    }

    public final Consumer<IOrder> partialFillAction() {
        return partialFillAction;
    }

    public final Consumer<IOrder> fillAction() {
        return fillAction;
    }

    public interface SubmitOption {
        public SubmitOption onError(Consumer<Throwable> errorAction);

        public SubmitOption onSubmitReject(Consumer<IOrder> submitRejectAction);

        public SubmitOption onFillReject(Consumer<IOrder> fillRejectAction);

        public SubmitOption onSubmitOK(Consumer<IOrder> submitOKAction);

        public SubmitOption onPartialFill(Consumer<IOrder> partialFillAction);

        public SubmitOption onFill(Consumer<IOrder> fillAction);

        public SubmitBuilder build();
    }

    private SubmitBuilder(final Builder builder) {
        orderParams = builder.orderParams;
        errorAction = builder.errorAction;
        submitRejectAction = builder.submitRejectAction;
        fillRejectAction = builder.fillRejectAction;
        submitOKAction = builder.submitOKAction;
        partialFillAction = builder.partialFillAction;
        fillAction = builder.fillAction;
    }

    public static final SubmitOption forOrderParams(final OrderParams orderParams) {
        return new Builder(checkNotNull(orderParams));
    }

    private static class Builder implements SubmitOption {

        private final OrderParams orderParams;
        private Consumer<Throwable> errorAction = t -> {};
        private Consumer<IOrder> submitRejectAction = o -> {};
        private Consumer<IOrder> fillRejectAction = o -> {};
        private Consumer<IOrder> submitOKAction = o -> {};
        private Consumer<IOrder> partialFillAction = o -> {};
        private Consumer<IOrder> fillAction = o -> {};

        private Builder(final OrderParams orderParams) {
            this.orderParams = orderParams;
        }

        @Override
        public SubmitOption onError(final Consumer<Throwable> errorAction) {
            this.errorAction = checkNotNull(errorAction);
            return this;
        }

        @Override
        public SubmitOption onSubmitReject(final Consumer<IOrder> submitRejectAction) {
            this.submitRejectAction = checkNotNull(submitRejectAction);
            return this;
        }

        @Override
        public SubmitOption onFillReject(final Consumer<IOrder> fillRejectAction) {
            this.fillRejectAction = checkNotNull(fillRejectAction);
            return this;
        }

        @Override
        public SubmitOption onSubmitOK(final Consumer<IOrder> submitOKAction) {
            this.submitOKAction = checkNotNull(submitOKAction);
            return this;
        }

        @Override
        public SubmitOption onPartialFill(final Consumer<IOrder> partialFillAction) {
            this.partialFillAction = checkNotNull(partialFillAction);
            return this;
        }

        @Override
        public SubmitOption onFill(final Consumer<IOrder> fillAction) {
            this.fillAction = checkNotNull(fillAction);
            return this;
        }

        @Override
        public SubmitBuilder build() {
            return new SubmitBuilder(this);
        }
    }
}
