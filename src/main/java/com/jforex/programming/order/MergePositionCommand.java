package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;

public class MergePositionCommand {

    private final Instrument instrument;
    private final MergeCommandWithParent mergeCommandWithParent;

    public interface MergeOption {

        MergeCommandWithParent.MergeOption<BuildOption> withMergeOption();

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

    public final MergeCommandWithParent mergeCommandWithParent() {
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

        private MergeCommandWithParent mergeCommandWithParent;
        private MergeCommandWithParent.MergeOption<BuildOption> option;
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
            this.mergeCommandWithParent = (MergeCommandWithParent) mergeCommandWithParent;
        }

        @Override
        public MergeCommandWithParent.MergeOption<BuildOption> withMergeOption() {
            option = MergeCommandWithParent.newBuilder(this, mergeOrderLabel);
            return option;
        }
    }
}
