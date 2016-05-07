package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderStaticUtil;

import com.dukascopy.api.IOrder;

public class PositionOrders {

    private enum OrderProcessState {
        IDLE,
        ACTIVE
    }

    private final ConcurrentMap<IOrder, OrderProcessState> orderRepository =
            new MapMaker().weakKeys().makeMap();

    public void add(final IOrder order) {
        orderRepository.put(order, OrderProcessState.IDLE);
    }

    public void remove(final IOrder order) {
        orderRepository.remove(order);
    }

    public boolean contains(final IOrder order) {
        return orderRepository.containsKey(order);
    }

    public void markAllActive() {
        orderRepository.replaceAll((k, v) -> OrderProcessState.ACTIVE);
    }

    public OrderDirection direction() {
        return OrderStaticUtil.combinedDirection(filter(isFilled));
    }

    public double signedExposure() {
        return filter(isFilled)
                .stream()
                .mapToDouble(OrderStaticUtil::signedAmount)
                .sum();
    }

    public Collection<IOrder> filter(final Predicate<IOrder> orderPredicate) {
        return orderRepository
                .keySet()
                .stream()
                .filter(orderPredicate)
                .collect(toList());
    }

    public Collection<IOrder> filterIdle(final Predicate<IOrder> orderPredicate) {
        return orderRepository
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == OrderProcessState.IDLE)
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()))
                .keySet();
    }

    public Collection<IOrder> orders() {
        return ImmutableSet.copyOf(orderRepository.keySet());
    }
}
