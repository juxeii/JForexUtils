package com.jforex.programming.position;

import com.dukascopy.api.IOrder;

public interface PositionChange {

    public void addOrder(final IOrder order);

    public void markAllOrdersActive();
}
