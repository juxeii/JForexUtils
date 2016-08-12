package com.jforex.programming.quote;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public final class BarParams {

    private final Instrument instrument;
    private final Period period;
    private final OfferSide offerSide;

    public interface AndPeriod {
        public AndOfferSide period(Period period);
    }

    public interface AndOfferSide {
        public BarParams offerSide(OfferSide offerSide);
    }

    private BarParams(final Builder builder) {
        instrument = builder.instrument;
        period = builder.period;
        offerSide = builder.offerSide;
    }

    public static final AndPeriod forInstrument(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
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
        public final AndOfferSide period(final Period period) {
            this.period = checkNotNull(period);
            return this;
        }

        @Override
        public final BarParams offerSide(final OfferSide offerSide) {
            this.offerSide = checkNotNull(offerSide);
            return new BarParams(this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((instrument == null) ? 0 : instrument.hashCode());
        result = prime * result + ((offerSide == null) ? 0 : offerSide.hashCode());
        result = prime * result + ((period == null) ? 0 : period.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BarParams other = (BarParams) obj;
        if (instrument != other.instrument)
            return false;
        if (offerSide != other.offerSide)
            return false;
        if (period == null) {
            if (other.period != null)
                return false;
        } else if (!period.equals(other.period))
            return false;
        return true;
    }
}
