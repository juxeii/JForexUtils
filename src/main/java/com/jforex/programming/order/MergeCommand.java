package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;

public class MergeCommand {

    private final Collection<IOrder> toMergeOrders;
    private MergeCommandWithParent mergeCommandWithParent;

    public interface MergeOption {

        MergeCommandWithParent.MergeOption withMergeOption();

        public MergeCommand build();
    }

    public interface BuildOption {

        public MergeCommand build();
    }

    private MergeCommand(final Builder builder) {
        toMergeOrders = builder.toMergeOrders;
    }

    public final Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public static MergeOption newBuilder(final String mergeOrderLabel,
                                         final Collection<IOrder> toMergeOrders) {
        return new Builder(mergeOrderLabel, toMergeOrders);
    }

    public static class Builder implements
                                BuildOption,
                                CommandParent<BuildOption, MergeCommandWithParent>,
                                MergeOption {

        private MergeCommandWithParent mergeCommandWithParent;
        private MergeCommandWithParent.MergeOption mergeChild;
        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        public Builder(final String mergeOrderLabel,
                       final Collection<IOrder> toMergeOrders) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
        }

        @Override
        public MergeCommand build() {
            return new MergeCommand(this);
        }

        @Override
        public com.jforex.programming.order.MergeCommandWithParent.MergeOption withMergeOption() {
            mergeChild = MergeCommandWithParent.newBuilder(this, mergeOrderLabel);
            return mergeChild;
        }

        @Override
        public BuildOption addChild(final MergeCommandWithParent mergeCommandWithParent) {
            this.mergeCommandWithParent = mergeCommandWithParent;
            return this;
        }
    }
}
