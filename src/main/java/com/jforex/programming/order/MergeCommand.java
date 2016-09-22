package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;

public class MergeCommand {

    private final Collection<IOrder> toMergeOrders;
    private final CommonMergeCommand mergeCommandWithParent;

    public interface MergeOption {

        CommonMergeCommand.MergeOption<BuildOption> withMergeOption();

        public MergeCommand build();
    }

    public interface BuildOption {

        public MergeCommand build();
    }

    private MergeCommand(final Builder builder) {
        toMergeOrders = builder.toMergeOrders;
        mergeCommandWithParent = builder.mergeCommandWithParent;
    }

    public final Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public final CommonMergeCommand commonMergeCommand() {
        return mergeCommandWithParent;
    }

    public static MergeOption newBuilder(final String mergeOrderLabel,
                                         final Collection<IOrder> toMergeOrders) {
        return new Builder(mergeOrderLabel, toMergeOrders);
    }

    private static class Builder implements
                                 BuildOption,
                                 CommandParent<BuildOption>,
                                 MergeOption {

        private CommonMergeCommand mergeCommandWithParent;
        private CommonMergeCommand.MergeOption<BuildOption> option;
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
        public void addChild(final Object mergeCommandWithParent) {
            this.mergeCommandWithParent = (CommonMergeCommand) mergeCommandWithParent;
        }

        @Override
        public CommonMergeCommand.MergeOption<BuildOption> withMergeOption() {
            option = CommonMergeCommand.newBuilder(this, mergeOrderLabel);
            return option;
        }
    }
}
