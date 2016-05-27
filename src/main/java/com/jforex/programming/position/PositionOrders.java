package com.jforex.programming.position;

import java.util.Set;
import java.util.function.Predicate;

import com.jforex.programming.order.OrderDirection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

public interface PositionOrders {

    public Instrument instrument();

    public int size();

    public boolean contains(final IOrder order);

    public OrderDirection direction();

    public double signedExposure();

    public Set<IOrder> orders();

    public Set<IOrder> filterOrders(final Predicate<IOrder> orderPredicate);

    public Set<IOrder> notProcessingOrders(final Predicate<IOrder> orderPredicate);

    public Set<IOrder> filledOrders();

    public Set<IOrder> filledOrOpenedOrders();
}
