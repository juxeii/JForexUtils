package com.jforex.programming.quote;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;

public final class TickQuote {

    private final Instrument instrument;
    private final ITick tick;

    public TickQuote(final Instrument instrument,
                     final ITick tick) {
        this.instrument = instrument;
        this.tick = tick;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final ITick tick() {
        return tick;
    }
}
