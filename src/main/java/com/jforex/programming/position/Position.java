package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;
import static com.jforex.programming.order.event.OrderEventTypeSets.endOfOrderEventTypes;
import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.event.OrderEvent;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

import rx.Observable;

public class Position {

    private final Instrument instrument;

    private enum OrderProcessState {
        IDLE, ACTIVE
    }

    private final ConcurrentMap<IOrder, OrderProcessState> orderRepository =
            new MapMaker().weakKeys().makeMap();

    private static final Logger logger = LogManager.getLogger(Position.class);

    public Position(final Instrument instrument,
                    final Observable<OrderEvent> orderEventObservable) {
        this.instrument = instrument;

        orderEventObservable
                .filter(orderEvent -> orderEvent.order().getInstrument() == instrument)
                .filter(orderEvent -> contains(orderEvent.order()))
                .doOnNext(orderEvent -> logger.info("Received event " + orderEvent.type() + " for order "
                        + orderEvent.order().getLabel() + "in repository for " + instrument))
                .filter(orderEvent -> endOfOrderEventTypes.contains(orderEvent.type()))
                .doOnNext(orderEvent -> removeOrder(orderEvent.order()))
                .subscribe();
    }

    public final synchronized void addOrder(final IOrder order) {
        orderRepository.put(order, OrderProcessState.IDLE);
        logger.debug("Added order " + order.getLabel() + " to position " + instrument + " Orderstate: "
                + order.getState() + " repo size " + orderRepository.size());
    }

    public final synchronized void removeOrder(final IOrder order) {
        orderRepository.remove(order);
        logger.debug("Removed order " + order.getLabel() + " from position " + instrument + " Orderstate: "
                + order.getState() + " repo size " + orderRepository.size());
    }

    public final boolean contains(final IOrder order) {
        return orderRepository.containsKey(order);
    }

    public final int size() {
        return orderRepository.size();
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

    public Instrument instrument() {
        return instrument;
    }

    public Set<IOrder> filledOrders() {
        return filterIdle(isFilled);
    }

    public void markAllOrdersActive() {
        markAllActive();
    }

    public Set<IOrder> ordersToClose() {
        return filterIdle(isFilled.or(isOpened));
    }
}