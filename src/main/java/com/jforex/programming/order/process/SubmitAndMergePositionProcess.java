package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;

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
        public Option onRemoveSLReject(final Consumer<IOrder> setSLRejectAction) {
            putRemoveSLReject(setSLRejectAction);
            return this;
        }

        @Override
        public Option onRemoveTPReject(final Consumer<IOrder> setTPRejectAction) {
            putRemoveTPReject(setTPRejectAction);
            return this;
        }

        @Override
        public Option onRemoveSL(final Consumer<IOrder> changedSLAction) {
            putRemoveSL(changedSLAction);
            return this;
        }

        @Override
        public Option onRemoveTP(final Consumer<IOrder> changedTPAction) {
            putRemoveSL(changedTPAction);
            return this;
        }

        @Override
        public Option onMergeReject(final Consumer<IOrder> mergeRejectAction) {
            putMergeReject(mergeRejectAction);
            return this;
        }

        @Override
        public Option onMerge(final Consumer<IOrder> mergedAction) {
            putMerge(mergedAction);
            return this;
        }

        @Override
        public Option onMergeClose(final Consumer<IOrder> mergeClosedAction) {
            putMergeClose(mergeClosedAction);
            return this;
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
        public SubmitAndMergePositionProcess build() {
            return new SubmitAndMergePositionProcess(this);
        }
    }
}
