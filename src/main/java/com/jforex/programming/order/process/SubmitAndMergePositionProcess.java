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

    private static class Builder extends CommonBuilder<Builder> implements Option {

        private final OrderParams orderParams;
        private final String mergeOrderLabel;

        private Builder(final OrderParams orderParams,
                        final String mergeOrderLabel) {
            this.orderParams = orderParams;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public Option onRemoveSLReject(final Consumer<IOrder> changeSLRejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(changeSLRejectAction));
            return this;
        }

        @Override
        public Option onRemoveTPReject(final Consumer<IOrder> changeTPRejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(changeTPRejectAction));
            return this;
        }

        @Override
        public Option onRemoveSL(final Consumer<IOrder> changedSLAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(changedSLAction));
            return this;
        }

        @Override
        public Option onRemoveTP(final Consumer<IOrder> changedTPAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(changedTPAction));
            return this;
        }

        @Override
        public Option onMergeReject(final Consumer<IOrder> mergeRejectAction) {
            eventHandlerForType.put(OrderEventType.MERGE_REJECTED, checkNotNull(mergeRejectAction));
            return this;
        }

        @Override
        public Option onMerge(final Consumer<IOrder> mergeOKAction) {
            eventHandlerForType.put(OrderEventType.MERGE_OK, checkNotNull(mergeOKAction));
            return this;
        }

        @Override
        public Option onMergeClose(final Consumer<IOrder> mergeCloseOKAction) {
            eventHandlerForType.put(OrderEventType.MERGE_CLOSE_OK, checkNotNull(mergeCloseOKAction));
            return this;
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
