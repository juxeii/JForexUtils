package com.jforex.programming.builder;

import java.util.Set;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class BarQuoteParams {

    private final Set<Instrument> instruments;
    private final Period period;
    private final OfferSide offerSide;

    private BarQuoteParams(final Builder builder) {
        instruments = builder.instruments;
        period = builder.period;
        offerSide = builder.offerSide;
    }

    public final Set<Instrument> instruments() {
        return instruments;
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
        public BarQuoteParams offerSide(OfferSide offerSide);
    }

    public static AndPeriod forInstruments(final Set<Instrument> instruments) {
        return new Builder(instruments);
    }

    private static class Builder implements
            AndPeriod,
            AndOfferSide {

        private final Set<Instrument> instruments;
        private Period period;
        private OfferSide offerSide;

        private Builder(final Set<Instrument> instruments) {
            this.instruments = instruments;
        }

        @Override
        public AndOfferSide period(final Period period) {
            this.period = period;
            return this;
        }

        @Override
        public BarQuoteParams offerSide(final OfferSide offerSide) {
            this.offerSide = offerSide;
            return new BarQuoteParams(this);
        }
    }
}
