package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEventType;

public class SubmitAndMergePositionProcess extends OrderProcess {

    private final OrderParams orderParams;
    private final String mergeOrderLabel;

    public interface Option extends SubmitOption<Option>, MergeOption<Option> {
        public SubmitAndMergePositionProcess build();
    }

    private SubmitAndMergePositionProcess(final Builder builder) {
        super(builder);
        orderParams = builder.orderParams;
        mergeOrderLabel = builder.mergeOrderLabel;
    }

    public final OrderParams orderParams() {
        return orderParams;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public static final Option forParams(final OrderParams orderParams,
                                         final String mergeOrderLabel) {
        return new Builder(checkNotNull(orderParams), checkNotNull(mergeOrderLabel));
    }

    private static class Builder extends CommonMergeProcess<Builder> implements Option {

        private final OrderParams orderParams;

        private Builder(final OrderParams orderParams,
                        final String mergeOrderLabel) {
            super(mergeOrderLabel);
            this.orderParams = orderParams;
        }

        @Override
        public Option onSubmitReject(final Consumer<IOrder> submitRejectAction) {
            eventHandlerForType.put(OrderEventType.SUBMIT_REJECTED, checkNotNull(submitRejectAction));
            return this;
        }

        @Override
        public Option onFillReject(final Consumer<IOrder> fillRejectAction) {
            eventHandlerForType.put(OrderEventType.FILL_REJECTED, checkNotNull(fillRejectAction));
            return this;
        }

        @Override
        public Option onSubmitOK(final Consumer<IOrder> submitOKAction) {
            eventHandlerForType.put(OrderEventType.SUBMIT_OK, checkNotNull(submitOKAction));
            eventHandlerForType.put(OrderEventType.SUBMIT_CONDITIONAL_OK, checkNotNull(submitOKAction));
            return this;
        }

        @Override
        public Option onPartialFill(final Consumer<IOrder> partialFillAction) {
            eventHandlerForType.put(OrderEventType.PARTIAL_FILL_OK, checkNotNull(partialFillAction));
            return this;
        }

        @Override
        public Option onFill(final Consumer<IOrder> fillAction) {
            eventHandlerForType.put(OrderEventType.FULLY_FILLED, checkNotNull(fillAction));
            return this;
        }

        @Override
        public SubmitAndMergePositionProcess build() {
            return new SubmitAndMergePositionProcess(this);
        }
    }
}
