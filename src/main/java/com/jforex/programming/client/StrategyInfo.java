package com.jforex.programming.client;

public final class StrategyInfo {

    private final long processID;
    private final StrategyState state;

    public StrategyInfo(final long processID,
                        final StrategyState state) {
        this.processID = processID;
        this.state = state;
    }

    public final long processID() {
        return processID;
    }

    public final StrategyState state() {
        return state;
    }
}
