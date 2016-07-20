package com.jforex.programming.client;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StrategyRunData {

    private final long processID;
    private final StrategyRunState state;

    public StrategyRunData(final long processID,
                           final StrategyRunState state) {
        this.processID = processID;
        this.state = checkNotNull(state);
    }

    public final long processID() {
        return processID;
    }

    public final StrategyRunState state() {
        return state;
    }
}
