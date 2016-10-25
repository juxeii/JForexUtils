package com.jforex.programming.quote;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
        checkNotNull(instrument);

        return new Builder(instrument);
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
            checkNotNull(period);

            this.period = period;
            return this;
        }

        @Override
        public final BarParams offerSide(final OfferSide offerSide) {
            checkNotNull(offerSide);

            this.offerSide = offerSide;
            return new BarParams(this);
        }
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(instrument);
        builder.append(period);
        builder.append(offerSide);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof BarParams))
            return false;

        final BarParams other = (BarParams) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(instrument, other.instrument);
        builder.append(period, other.period);
        builder.append(offerSide, other.offerSide);

        return builder.isEquals();
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
        toStringBuilder.append("instrument", instrument);
        toStringBuilder.append("period", period);
        toStringBuilder.append("offerSide", offerSide);

        return toStringBuilder.toString();
    }
}
