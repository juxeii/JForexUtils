package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isCanceled;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.event.OrderEventTypeSets.createEvents;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public class Position implements PositionOrders {

    private final Instrument instrument;
    private final ConcurrentMap<IOrder, OrderProcessState> orderRepository =
            new MapMaker().weakKeys().makeMap();

    private static final Logger logger = LogManager.getLogger(Position.class);

    public Position(final Instrument instrument,
                    final Observable<OrderEvent> orderEventObservable) {
        this.instrument = instrument;

        observeClosedOrdersForRemoval(orderEventObservable);
        observeCreatedOrdersForInsertion(orderEventObservable);
    }

    private void observeClosedOrdersForRemoval(final Observable<OrderEvent> orderEventObservable) {
        orderEventObservable
            .map(OrderEvent::order)
            .filter(this::contains)
            .filter(isClosed.or(isCanceled)::test)
            .doOnNext(this::removeOrder)
            .subscribe();
    }

    private void observeCreatedOrdersForInsertion(final Observable<OrderEvent> orderEventObservable) {
        orderEventObservable
            .filter(orderEvent -> createEvents.contains(orderEvent.type()))
            .filter(OrderEvent::isInternal)
            .map(OrderEvent::order)
            .doOnNext(this::addOrder)
            .subscribe();
    }

    private synchronized void removeOrder(final IOrder order) {
        orderRepository.remove(order);
        logger.debug("Removed order " + order.getLabel() + " from position " + instrument
                + " Orderstate: " + order.getState() + " repo size " + orderRepository.size());
    }

    public synchronized void addOrder(final IOrder order) {
        orderRepository.put(order, OrderProcessState.IDLE);
        logger.debug("Added order " + order.getLabel() + " to position " + instrument
                + " Orderstate: " + order.getState() + " repo size " + orderRepository.size());
    }

    public synchronized void markOrderActive(final IOrder order) {
        markOrder(order, OrderProcessState.ACTIVE);
    }

    public synchronized void markOrdersActive(final Collection<IOrder> orders) {
        orders.forEach(this::markOrderActive);
    }

    public synchronized void markOrderIdle(final IOrder order) {
        markOrder(order, OrderProcessState.IDLE);
    }

    public synchronized void markOrdersIdle(final Collection<IOrder> orders) {
        orders.forEach(this::markOrderIdle);
    }

    private synchronized void markOrder(final IOrder order,
                                        final OrderProcessState state) {
        orderRepository.computeIfPresent(order, (k, v) -> state);
    }

    @Override
    public Instrument instrument() {
        return instrument;
    }

    @Override
    public boolean contains(final IOrder order) {
        return orderRepository.containsKey(order);
    }

    @Override
    public int size() {
        return orderRepository.size();
    }

    @Override
    public OrderDirection direction() {
        return OrderStaticUtil.combinedDirection(filter(isFilled));
    }

    @Override
    public double signedExposure() {
        return filter(isFilled)
            .stream()
            .mapToDouble(OrderStaticUtil::signedAmount)
            .sum();
    }

    @Override
    public Set<IOrder> all() {
        return ImmutableSet.copyOf(orderRepository.keySet());
    }

    @Override
    public Set<IOrder> filter(final Predicate<IOrder> orderPredicate) {
        return orderRepository
            .keySet()
            .stream()
            .filter(orderPredicate)
            .collect(toSet());
    }

    @Override
    public Set<IOrder> filled() {
        return notProcessingOrders(isFilled);
    }

    @Override
    public Set<IOrder> filledOrOpened() {
        return notProcessingOrders(isFilled.or(isOpened));
    }

    public Set<IOrder> notProcessingOrders(final Predicate<IOrder> orderPredicate) {
        return orderRepository
            .entrySet()
            .stream()
            .filter(entry -> orderPredicate.test(entry.getKey()))
            .filter(entry -> entry.getValue() == OrderProcessState.IDLE)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue))
            .keySet();
    }
}
