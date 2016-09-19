package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;

public class CancelSLTPCommand {

    private final Instrument instrument;
    private final CancelSLPositionCommand cancelSLCommand;
    private final CancelTPPositionCommand cancelTPCommand;
    private final ExecutionMode executionMode;

    public enum ExecutionMode {
        ConcatSLAndTP,
        ConcatTPAndSL,
        MergeSLAndTP
    }

    public interface CancelSLTPOption {

        public BuildSLTPOption concatSLAndTP(CancelSLPositionCommand cancelSLCommand,
                                             CancelTPPositionCommand cancelTPCommand);

        public BuildSLTPOption concatTPAndSL(CancelTPPositionCommand cancelTPCommand,
                                             CancelSLPositionCommand cancelSLCommand);

        public BuildSLTPOption mergeSLAndTP(CancelSLPositionCommand cancelSLCommand,
                                            CancelTPPositionCommand cancelTPCommand);
    }

    public interface BuildSLTPOption {

        public CancelSLTPCommand build();
    }

    private CancelSLTPCommand(final Builder builder) {
        instrument = builder.instrument;
        cancelSLCommand = builder.cancelSLCommand;
        cancelTPCommand = builder.cancelTPCommand;
        executionMode = builder.executionMode;
    }

    public static final CancelSLTPOption with(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final CancelSLPositionCommand cancelSLCommand() {
        return cancelSLCommand;
    }

    public final CancelTPPositionCommand cancelTPCommand() {
        return cancelTPCommand;
    }

    public final ExecutionMode executionMode() {
        return executionMode;
    }

    private static class Builder implements CancelSLTPOption, BuildSLTPOption {

        private final Instrument instrument;
        private CancelSLPositionCommand cancelSLCommand;
        private CancelTPPositionCommand cancelTPCommand;
        private ExecutionMode executionMode;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
        }

        @Override
        public BuildSLTPOption concatSLAndTP(final CancelSLPositionCommand cancelSLCommand,
                                             final CancelTPPositionCommand cancelTPCommand) {
            return setValues(cancelSLCommand,
                             cancelTPCommand,
                             ExecutionMode.ConcatSLAndTP);
        }

        @Override
        public BuildSLTPOption concatTPAndSL(final CancelTPPositionCommand cancelTPCommand,
                                             final CancelSLPositionCommand cancelSLCommand) {
            return setValues(cancelSLCommand,
                             cancelTPCommand,
                             ExecutionMode.ConcatTPAndSL);
        }

        @Override
        public BuildSLTPOption mergeSLAndTP(final CancelSLPositionCommand cancelSLCommand,
                                            final CancelTPPositionCommand cancelTPCommand) {
            return setValues(cancelSLCommand,
                             cancelTPCommand,
                             ExecutionMode.MergeSLAndTP);
        }

        private BuildSLTPOption setValues(final CancelSLPositionCommand cancelSLCommand,
                                          final CancelTPPositionCommand cancelTPCommand,
                                          final ExecutionMode executionMode) {
            checkNotNull(cancelSLCommand);
            checkNotNull(cancelTPCommand);

            this.cancelSLCommand = cancelSLCommand;
            this.cancelTPCommand = cancelTPCommand;
            this.executionMode = executionMode;
            return this;
        }

        @Override
        public CancelSLTPCommand build() {
            return new CancelSLTPCommand(this);
        }
    }
}
