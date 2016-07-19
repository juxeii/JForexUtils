package com.jforex.programming.position;

import java.util.Set;
import java.util.function.Predicate;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderDirection;

public interface PositionOrders {

    public Instrument instrument();

    public int size();

    public boolean contains(final IOrder order);

    public OrderDirection direction();

    public double signedExposure();

    public Set<IOrder> all();

    public Set<IOrder> filled();

    public Set<IOrder> filledOrOpened();

    public Set<IOrder> filter(final Predicate<IOrder> orderPredicate);
}
