package com.jforex.programming.quote;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;

public final class TickQuote {

    private final Instrument instrument;
    private final ITick tick;

    public TickQuote(final Instrument instrument,
                     final ITick tick) {
        this.instrument = instrument;
        this.tick = tick;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final ITick tick() {
        return tick;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(instrument);
        builder.append(tick);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof TickQuote))
            return false;

        final TickQuote other = (TickQuote) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(instrument, other.instrument);
        builder.append(tick, other.tick);

        return builder.isEquals();
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
        toStringBuilder.append("instrument", instrument);
        toStringBuilder.append("tick", tick);

        return toStringBuilder.toString();
    }
}
