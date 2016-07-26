package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isCanceled;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
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

        orderEventObservable
                .map(OrderEvent::order)
                .filter(this::contains)
                .filter(isClosed.or(isCanceled)::test)
                .doOnNext(this::removeOrder)
                .subscribe();
    }

    public synchronized void addOrder(final IOrder order) {
        orderRepository.put(order, OrderProcessState.IDLE);
        logger.debug("Added order " + order.getLabel() + " to position " + instrument
                + " Orderstate: "
                + order.getState() + " repo size " + orderRepository.size());
    }

    public synchronized void markOrdersActive(final Collection<IOrder> orders) {
        orders.forEach(order -> {
            if (orderRepository.containsKey(order))
                orderRepository.put(order, OrderProcessState.ACTIVE);
        });
    }

    public synchronized void markOrdersIdle(final Collection<IOrder> orders) {
        orders.forEach(order -> {
            if (orderRepository.containsKey(order))
                orderRepository.put(order, OrderProcessState.IDLE);
        });
    }

    private synchronized void removeOrder(final IOrder order) {
        orderRepository.remove(order);
        logger.debug("Removed order " + order.getLabel() + " from position " + instrument
                + " Orderstate: "
                + order.getState() + " repo size " + orderRepository.size());
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