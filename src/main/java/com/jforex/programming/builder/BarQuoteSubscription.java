package com.jforex.programming.builder;

import java.util.Set;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class BarQuoteSubscription {

    private final Set<Instrument> instruments;
    private final Period period;
    private final OfferSide offerSide;

    private BarQuoteSubscription(final Builder builder) {
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
        public Build offerSide(OfferSide offerSide);
    }

    public interface Build {
        public BarQuoteSubscription build();
    }

    public static AndPeriod forInstruments(final Set<Instrument> instruments) {
        return new Builder(instruments);
    }

    private static class Builder implements
            AndPeriod,
            AndOfferSide,
            Build {

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
        public Build offerSide(final OfferSide offerSide) {
            this.offerSide = offerSide;
            return this;
        }

        @Override
        public BarQuoteSubscription build() {
            return new BarQuoteSubscription(this);
        }
    }
}
