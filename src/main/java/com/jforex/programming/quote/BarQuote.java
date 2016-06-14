package com.jforex.programming.quote;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public final class BarQuote {

    private final IBar bar;
    private final Instrument instrument;
    private final Period period;
    private final OfferSide offerSide;

    public BarQuote(final IBar bar,
                    final Instrument instrument,
                    final Period period,
                    final OfferSide offerSide) {
        this.bar = bar;
        this.instrument = instrument;
        this.period = period;
        this.offerSide = offerSide;
    }

    public final IBar bar() {
        return bar;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final Period period() {
        return period;
    }

    public final OfferSide offerSide() {
        return offerSide;
    }
}
