package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderStaticUtil;

public final class PositionOrders {

    private enum OrderProcessState {
        IDLE, ACTIVE
    }

    private final ConcurrentMap<IOrder, OrderProcessState> orderRepository =
            new MapMaker().weakKeys().makeMap();

    public final synchronized void add(final IOrder order) {
        orderRepository.put(order, OrderProcessState.IDLE);
    }

    public final synchronized void remove(final IOrder order) {
        orderRepository.remove(order);
    }

    public final boolean contains(final IOrder order) {
        return orderRepository.containsKey(order);
    }

    public final synchronized void markAllActive() {
        orderRepository.replaceAll((k, v) -> OrderProcessState.ACTIVE);
    }

    public final OrderDirection direction() {
        return OrderStaticUtil.combinedDirection(filter(isFilled));
    }

    public final double signedExposure() {
        return filter(isFilled)
                .stream()
                .mapToDouble(OrderStaticUtil::signedAmount)
                .sum();
    }

    public final Set<IOrder> filter(final Predicate<IOrder> orderPredicate) {
        return orderRepository
                .keySet()
                .stream()
                .filter(orderPredicate)
                .collect(toSet());
    }

    public final Set<IOrder> filterIdle(final Predicate<IOrder> orderPredicate) {
        return orderRepository
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == OrderProcessState.IDLE)
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()))
                .keySet();
    }

    public Set<IOrder> orders() {
        return ImmutableSet.copyOf(orderRepository.keySet());
    }
}
