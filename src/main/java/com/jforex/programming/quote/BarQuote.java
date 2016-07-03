package com.jforex.programming.quote;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public final class BarQuote {

    private final BarQuoteParams barQuoteParams;
    private final IBar bar;

    public BarQuote(final BarQuoteParams barQuoteParams,
                    final IBar bar) {
        this.barQuoteParams = barQuoteParams;
        this.bar = bar;
    }

    public final Instrument instrument() {
        return barQuoteParams.instrument();
    }

    public final Period period() {
        return barQuoteParams.period();
    }

    public final OfferSide offerSide() {
        return barQuoteParams.offerSide();
    }

    public final IBar bar() {
        return bar;
    }
}
