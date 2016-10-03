package com.jforex.programming.order.call;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.dukascopy.api.IOrder;

public final class OrderCallRequest {

    private final IOrder order;
    private final OrderCallReason reason;

    public OrderCallRequest(final IOrder order,
                            final OrderCallReason reason) {
        this.order = order;
        this.reason = reason;
    }

    public final IOrder order() {
        return order;
    }

    public final OrderCallReason reason() {
        return reason;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(order);
        hashCodeBuilder.append(reason);

        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof OrderCallRequest))
            return false;
        final OrderCallRequest other = (OrderCallRequest) obj;
        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(order, other.order);
        equalsBuilder.append(reason, other.reason);

        return equalsBuilder.isEquals();
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
        toStringBuilder.append("order", order);
        toStringBuilder.append("reason", reason);

        return toStringBuilder.toString();
    }
}
