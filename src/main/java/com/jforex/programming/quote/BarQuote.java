package com.jforex.programming.quote;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(barParams);
        builder.append(bar);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof BarQuote))
            return false;

        final BarQuote other = (BarQuote) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(barParams, other.barParams);
        builder.append(bar, other.bar);
        return builder.isEquals();
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
        toStringBuilder.append("barParams", barParams);
        toStringBuilder.append("bar", bar);

        return toStringBuilder.toString();
    }
}
