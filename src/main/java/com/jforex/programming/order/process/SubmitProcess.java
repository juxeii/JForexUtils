package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;

public class SubmitProcess extends OrderProcess {

    private final OrderParams orderParams;

    public interface Option extends SubmitOption<Option> {
        public SubmitProcess build();
    }

    private SubmitProcess(final Builder builder) {
        super(builder);
        orderParams = builder.orderParams;
    }

    public final OrderParams orderParams() {
        return orderParams;
    }

    public static final Option forOrderParams(final OrderParams orderParams) {
        return new Builder(checkNotNull(orderParams));
    }

    private static class Builder extends CommonBuilder<Builder> implements Option {

        private final OrderParams orderParams;

        private Builder(final OrderParams orderParams) {
            this.orderParams = orderParams;
        }

        @Override
        public Option onSubmitReject(final Consumer<IOrder> submitRejectAction) {
            putSubmitReject(submitRejectAction);
            return this;
        }

        @Override
        public Option onFillReject(final Consumer<IOrder> fillRejectAction) {
            putFillReject(fillRejectAction);
            return this;
        }

        @Override
        public Option onSubmitOK(final Consumer<IOrder> submittedAction) {
            putSubmitOK(submittedAction);
            return this;
        }

        @Override
        public Option onPartialFill(final Consumer<IOrder> partialFilledAction) {
            putPartialFill(partialFilledAction);
            return this;
        }

        @Override
        public Option onFill(final Consumer<IOrder> filledAction) {
            putFill(filledAction);
            return this;
        }

        @Override
        public SubmitProcess build() {
            return new SubmitProcess(this);
        }
    }
}
