package com.jforex.programming.client;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class StrategyRunData {

    private final long processID;
    private final StrategyRunState state;

    public StrategyRunData(final long processID,
                           final StrategyRunState state) {
        this.processID = processID;
        this.state = checkNotNull(state);
    }

    public final long processID() {
        return processID;
    }

    public final StrategyRunState state() {
        return state;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(processID);
        builder.append(state);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof StrategyRunData))
            return false;

        final StrategyRunData other = (StrategyRunData) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(processID, other.processID);
        builder.append(state, other.state);

        return builder.isEquals();
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
        toStringBuilder.append("processID", processID);
        toStringBuilder.append("state", state);

        return toStringBuilder.toString();
    }
}
