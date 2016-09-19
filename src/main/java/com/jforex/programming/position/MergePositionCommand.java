package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;

public class MergePositionCommand {

    private final Instrument instrument;
    private final String mergeOrderLabel;
    private final CancelSLTPCommand cancelSLTPCommand;

    public interface MergePositionOption {

        public MergePositionOption withCancelSLAndTP(CancelSLTPCommand cancelSLTPCommand);

        public MergePositionCommand build();
    }

    private MergePositionCommand(final Builder builder) {
        instrument = builder.instrument;
        mergeOrderLabel = builder.mergeOrderLabel;
        cancelSLTPCommand = builder.cancelSLTPCommand;
    }

    public static final MergePositionOption with(final Instrument instrument,
                                                 final String mergeOrderLabel) {
        return new Builder(checkNotNull(instrument),
                           checkNotNull(mergeOrderLabel));
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final CancelSLTPCommand cancelSLTPCommand() {
        return cancelSLTPCommand;
    }

    private static class Builder implements MergePositionOption {

        private final Instrument instrument;
        private final String mergeOrderLabel;
        private CancelSLTPCommand cancelSLTPCommand;

        private Builder(final Instrument instrument,
                        final String mergeOrderLabel) {
            this.instrument = instrument;
            this.mergeOrderLabel = mergeOrderLabel;
        }

        @Override
        public MergePositionOption withCancelSLAndTP(final CancelSLTPCommand cancelSLTPCommand) {
            this.cancelSLTPCommand = checkNotNull(cancelSLTPCommand);
            return this;
        }

        @Override
        public MergePositionCommand build() {
            return new MergePositionCommand(this);
        }
    }
}
