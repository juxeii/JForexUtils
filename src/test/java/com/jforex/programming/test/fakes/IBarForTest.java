package com.jforex.programming.test.fakes;

import com.dukascopy.api.IBar;

public class IBarForTest implements IBar {

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public double getOpen() {
        return 0;
    }

    @Override
    public double getClose() {
        return 0;
    }

    @Override
    public double getLow() {
        return 0;
    }

    @Override
    public double getHigh() {
        return 0;
    }

    @Override
    public double getVolume() {
        return 0;
    }
}
