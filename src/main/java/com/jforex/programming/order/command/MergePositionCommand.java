package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;

public class MergePositionCommand {

    private final Instrument instrument;
    private final MergeCommand mergeCommand;

    public MergePositionCommand(final Instrument instrument,
                                final MergeCommand mergeCommand) {
        this.instrument = checkNotNull(instrument);
        this.mergeCommand = checkNotNull(mergeCommand);
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final MergeCommand mergeCommand() {
        return mergeCommand;
    }
}
