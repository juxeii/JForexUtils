package com.jforex.programming.builder;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class BarQuoteFilter {

    private final Instrument instrument;
    private final Period period;
    private final OfferSide offerSide;

    private BarQuoteFilter(final Builder builder) {
        instrument = builder.instrument;
        period = builder.period;
        offerSide = builder.offerSide;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public Period period() {
        return period;
    }

    public OfferSide offerSide() {
        return offerSide;
    }

    public interface AndPeriod {
        public AndOfferSide period(Period period);
    }

    public interface AndOfferSide {
        public BarQuoteFilter offerSide(OfferSide offerSide);
    }

    public static AndPeriod forInstrument(final Instrument instrument) {
        return new Builder(instrument);
    }

    private static class Builder implements
            AndPeriod,
            AndOfferSide {

        private final Instrument instrument;
        private Period period;
        private OfferSide offerSide;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
        }

        @Override
        public AndOfferSide period(final Period period) {
            this.period = period;
            return this;
        }

        @Override
        public BarQuoteFilter offerSide(final OfferSide offerSide) {
            this.offerSide = offerSide;
            return new BarQuoteFilter(this);
        }
    }
}
