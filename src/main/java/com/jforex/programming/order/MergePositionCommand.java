package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;

public class MergePositionCommand {

    private final Instrument instrument;
    private final CommonMergeCommand mergeCommandWithParent;

    public interface MergeOption {

        CommonMergeCommand.MergeOption<BuildOption> withMergeOption();

        public MergePositionCommand build();
    }

    public interface BuildOption {

        public MergePositionCommand build();
    }

    private MergePositionCommand(final Builder builder) {
        instrument = builder.instrument;
        mergeCommandWithParent = builder.mergeCommandWithParent;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final CommonMergeCommand commonMergeCommand() {
        return mergeCommandWithParent;
    }

    public static MergeOption newBuilder(final String mergeOrderLabel,
                                         final Instrument instrument) {
        return new Builder(checkNotNull(mergeOrderLabel), checkNotNull(instrument));
    }

    private static class Builder implements
                                 BuildOption,
                                 CommandParent<BuildOption>,
                                 MergeOption {

        private CommonMergeCommand mergeCommandWithParent;
        private CommonMergeCommand.MergeOption<BuildOption> option;
        private final String mergeOrderLabel;
        private final Instrument instrument;

        public Builder(final String mergeOrderLabel,
                       final Instrument instrument) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.instrument = instrument;
        }

        @Override
        public MergePositionCommand build() {
            return new MergePositionCommand(this);
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
