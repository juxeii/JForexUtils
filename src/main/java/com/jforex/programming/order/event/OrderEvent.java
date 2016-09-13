package com.jforex.programming.order.event;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.dukascopy.api.IOrder;

public final class OrderEvent {

    private final IOrder order;
    private final OrderEventType type;
    private final boolean isInternal;

    public OrderEvent(final IOrder order,
                      final OrderEventType type,
                      final boolean isInternal) {
        this.order = order;
        this.type = type;
        this.isInternal = isInternal;
    }

    public final IOrder order() {
        return order;
    }

    public final OrderEventType type() {
        return type;
    }

    public boolean isInternal() {
        return isInternal;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(order);
        builder.append(type);
        builder.append(isInternal);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof OrderEvent))
            return false;

        final OrderEvent other = (OrderEvent) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(order, other.order);
        builder.append(type, other.type);
        builder.append(isInternal, other.isInternal);

        return builder.isEquals();
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
        toStringBuilder.append("order", order);
        toStringBuilder.append("type", type);
        toStringBuilder.append("isInternal", isInternal);

        return toStringBuilder.toString();
    }
}
