package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;

public class OrderRepository {

    private final Set<IOrder> orders = Sets.newIdentityHashSet();

    public void add(final IOrder order) {
        orders.add(order);
    }

    public int size() {
        return orders.size();
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public void remove(final IOrder order) {
        orders.remove(order);
    }

    public void removeIf(final Predicate<? super IOrder> filter) {
        orders.removeIf(filter);
    }

    public boolean contains(final IOrder order) {
        return orders.contains(order);
    }

    public Collection<IOrder> filter(final Predicate<IOrder> orderPredicate) {
        return orders.stream()
                     .filter(orderPredicate)
                     .collect(toList());
    }

    public Collection<IOrder> filled() {
        return filter(isFilled);
    }

    public OrderDirection direction() {
        return OrderStaticUtil.combinedDirection(filter(isFilled));
    }

    public double signedExposure() {
        return filter(isFilled).stream()
                               .mapToDouble(OrderStaticUtil::signedAmount)
                               .sum();
    }
}
