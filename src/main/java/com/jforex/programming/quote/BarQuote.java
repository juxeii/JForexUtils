package com.jforex.programming.quote;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public final class BarQuote {

    private final IBar bar;
    private final BarParams barParams;

    public BarQuote(final IBar bar,
                    final BarParams barParams) {
        this.barParams = barParams;
        this.bar = bar;
    }

    public final IBar bar() {
        return bar;
    }

    public final BarParams barParams() {
        return barParams;
    }

    public final Instrument instrument() {
        return barParams.instrument();
    }

    public final Period period() {
        return barParams.period();
    }

    public final OfferSide offerSide() {
        return barParams.offerSide();
    }
}
