package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.SubmitOption;

public class SubmitProcess extends CommonProcess {

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

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final OrderParams orderParams;

        private Builder(final OrderParams orderParams) {
            this.orderParams = orderParams;
        }

        public Option onSubmitReject(final Consumer<IOrder> submitRejectAction) {
            eventHandlerForType.put(OrderEventType.SUBMIT_REJECTED, checkNotNull(submitRejectAction));
            return this;
        }

        public Option onFillReject(final Consumer<IOrder> fillRejectAction) {
            eventHandlerForType.put(OrderEventType.FILL_REJECTED, checkNotNull(fillRejectAction));
            return this;
        }

        public Option onSubmitOK(final Consumer<IOrder> submitOKAction) {
            eventHandlerForType.put(OrderEventType.SUBMIT_OK, checkNotNull(submitOKAction));
            eventHandlerForType.put(OrderEventType.SUBMIT_CONDITIONAL_OK, checkNotNull(submitOKAction));
            return this;
        }

        public Option onPartialFill(final Consumer<IOrder> partialFillAction) {
            eventHandlerForType.put(OrderEventType.PARTIAL_FILL_OK, checkNotNull(partialFillAction));
            return this;
        }

        public Option onFill(final Consumer<IOrder> fillAction) {
            eventHandlerForType.put(OrderEventType.FULLY_FILLED, checkNotNull(fillAction));
            return this;
        }

        @Override
        public SubmitProcess build() {
            return new SubmitProcess(this);
        }
    }
}
