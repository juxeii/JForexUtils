package com.jforex.programming.quote;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public final class BarQuote {

    private final Instrument instrument;
    private final Period period;
    private final IBar askBar;
    private final IBar bidBar;

    public BarQuote(final Instrument instrument,
                    final Period period,
                    final IBar askBar,
                    final IBar bidBar) {
        this.instrument = instrument;
        this.period = period;
        this.askBar = askBar;
        this.bidBar = bidBar;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final Period period() {
        return period;
    }

    public final IBar askBar() {
        return askBar;
    }

    public final IBar bidBar() {
        return bidBar;
    }
}
