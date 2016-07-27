package com.jforex.programming.quote;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public final class BarParams {

    private final Instrument instrument;
    private final Period period;
    private final OfferSide offerSide;

    private BarParams(final Builder builder) {
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
        public BarParams offerSide(OfferSide offerSide);
    }

    public static AndPeriod forInstrument(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
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
            this.period = checkNotNull(period);
            return this;
        }

        @Override
        public BarParams offerSide(final OfferSide offerSide) {
            this.offerSide = checkNotNull(offerSide);
            return new BarParams(this);
        }
    }
}
