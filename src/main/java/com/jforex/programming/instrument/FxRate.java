package com.jforex.programming.instrument;

import com.dukascopy.api.Instrument;

public class FxRate {

    private final double value;
    private final Instrument instrument;

    public FxRate(final double value,
                  final Instrument instrument) {
        this.value = value;
        this.instrument = instrument;
    }

    public double value() {
        return value;
    }

    public Instrument instrument() {
        return instrument;
    }
}
