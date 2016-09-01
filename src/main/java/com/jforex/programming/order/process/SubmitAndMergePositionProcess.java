package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.MergeOption;
import com.jforex.programming.order.process.option.SubmitOption;

public class SubmitAndMergePositionProcess extends CommonProcess {

    private final OrderParams orderParams;
    private final String mergeOrderLabel;

    public interface Option extends MergeOption<Option>, SubmitOption<Option> {

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

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final OrderParams orderParams;
        private final String mergeOrderLabel;

        private Builder(final OrderParams orderParams,
                        final String mergeOrderLabel) {
            this.orderParams = orderParams;
            this.mergeOrderLabel = mergeOrderLabel;
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

        public Option onRemoveSLReject(final Consumer<IOrder> removeSLRejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(removeSLRejectAction));
            return this;
        }

        public Option onRemoveTPReject(final Consumer<IOrder> removeTPRejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(removeTPRejectAction));
            return this;
        }

        public Option onRemoveSL(final Consumer<IOrder> removedSLAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(removedSLAction));
            return this;
        }

        public Option onRemoveTP(final Consumer<IOrder> removedTPAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(removedTPAction));
            return this;
        }

        public Option onMergeReject(final Consumer<IOrder> mergeRejectAction) {
            eventHandlerForType.put(OrderEventType.MERGE_REJECTED, checkNotNull(mergeRejectAction));
            return this;
        }

        public Option onMerge(final Consumer<IOrder> mergedAction) {
            eventHandlerForType.put(OrderEventType.MERGE_OK, checkNotNull(mergedAction));
            return this;
        }

        public Option onMergeClose(final Consumer<IOrder> mergeClosedAction) {
            eventHandlerForType.put(OrderEventType.MERGE_CLOSE_OK, checkNotNull(mergeClosedAction));
            return this;
        }

        @Override
        public SubmitAndMergePositionProcess build() {
            return new SubmitAndMergePositionProcess(this);
        }
    }
}
