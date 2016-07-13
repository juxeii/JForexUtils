package com.jforex.programming.test.fakes;

import com.dukascopy.api.ITick;

public class ITickForTest implements ITick {

    private final double bid;
    private final double ask;

    public ITickForTest(final double bid,
                        final double ask) {
        this.bid = bid;
        this.ask = ask;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public double getAsk() {
        return ask;
    }

    @Override
    public double getAskVolume() {
        return 0;
    }

    @Override
    public double[] getAskVolumes() {
        return null;
    }

    @Override
    public double[] getAsks() {
        return null;
    }

    @Override
    public double getBid() {
        return bid;
    }

    @Override
    public double getBidVolume() {
        return 0;
    }

    @Override
    public double[] getBidVolumes() {
        return null;
    }

    @Override
    public double[] getBids() {
        return null;
    }

    @Override
    public double getTotalAskVolume() {
        return 0;
    }

    @Override
    public double getTotalBidVolume() {
        return 0;
    }
}
