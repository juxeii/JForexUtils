package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEventType;

public class SubmitProcess extends OrderProcess {

    private final OrderParams orderParams;

    public interface SubmitOption extends CommonOption<SubmitOption> {
        public SubmitOption onSubmitReject(Consumer<IOrder> submitRejectAction);

        public SubmitOption onFillReject(Consumer<IOrder> fillRejectAction);

        public SubmitOption onSubmitOK(Consumer<IOrder> submitOKAction);

        public SubmitOption onPartialFill(Consumer<IOrder> partialFillAction);

        public SubmitOption onFill(Consumer<IOrder> fillAction);

        public SubmitProcess build();
    }

    private SubmitProcess(final Builder builder) {
        super(builder);
        orderParams = builder.orderParams;
    }

    public final OrderParams orderParams() {
        return orderParams;
    }

    public static final SubmitOption forOrderParams(final OrderParams orderParams) {
        return new Builder(checkNotNull(orderParams));
    }

    private static class Builder extends CommonProcess<Builder> implements SubmitOption {

        private final OrderParams orderParams;

        private Builder(final OrderParams orderParams) {
            this.orderParams = orderParams;
        }

        @Override
        public SubmitOption onSubmitReject(final Consumer<IOrder> submitRejectAction) {
            eventHandlerForType.put(OrderEventType.SUBMIT_REJECTED, checkNotNull(submitRejectAction));
            return this;
        }

        @Override
        public SubmitOption onFillReject(final Consumer<IOrder> fillRejectAction) {
            eventHandlerForType.put(OrderEventType.FILL_REJECTED, checkNotNull(fillRejectAction));
            return this;
        }

        @Override
        public SubmitOption onSubmitOK(final Consumer<IOrder> submitOKAction) {
            eventHandlerForType.put(OrderEventType.SUBMIT_OK, checkNotNull(submitOKAction));
            eventHandlerForType.put(OrderEventType.SUBMIT_CONDITIONAL_OK, checkNotNull(submitOKAction));
            return this;
        }

        @Override
        public SubmitOption onPartialFill(final Consumer<IOrder> partialFillAction) {
            eventHandlerForType.put(OrderEventType.PARTIAL_FILL_OK, checkNotNull(partialFillAction));
            return this;
        }

        @Override
        public SubmitOption onFill(final Consumer<IOrder> fillAction) {
            eventHandlerForType.put(OrderEventType.FULLY_FILLED, checkNotNull(fillAction));
            return this;
        }

        @Override
        public SubmitProcess build() {
            return new SubmitProcess(this);
        }
    }
}
