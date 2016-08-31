package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public class MergeProcess extends OrderProcess {

    private final String mergeOrderLabel;
    private final Collection<IOrder> toMergeOrders;

    public interface Option extends MergeOption<Option> {
        public MergeProcess build();
    }

    private MergeProcess(final Builder builder) {
        super(builder);
        mergeOrderLabel = builder.mergeOrderLabel;
        toMergeOrders = builder.toMergeOrders;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public static final Option forParams(final String mergeOrderLabel,
                                         final Collection<IOrder> toMergeOrders) {
        return new Builder(checkNotNull(mergeOrderLabel), checkNotNull(toMergeOrders));
    }

    private static class Builder extends CommonBuilder<Builder> implements Option {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        private Builder(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
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
        public MergeProcess build() {
            return new MergeProcess(this);
        }
    }
}
