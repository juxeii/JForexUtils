package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isCanceled;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.event.OrderEventTypeSets.createEvents;
import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class Position implements PositionOrders {

    private final Instrument instrument;
    private final ConcurrentMap<IOrder, Boolean> orderRepository =
            new MapMaker().weakKeys().makeMap();

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
            .filter(orderEvent -> orderEvent.order().getInstrument() == instrument)
            .filter(orderEvent -> createEvents.contains(orderEvent.type()))
            .filter(OrderEvent::isInternal)
            .map(OrderEvent::order)
            .doOnNext(this::addOrder)
            .subscribe();
    }

    private synchronized void removeOrder(final IOrder order) {
        orderRepository.remove(order);
    }

    private synchronized void addOrder(final IOrder order) {
        orderRepository.put(order, true);
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
    public String toString() {
        return "Position for " + instrument + " contains " + size() + " orders: \r\n"
                + "Filled orders: " + filled().toString() + "\r\n"
                + "Opened orders: " + opened().toString() + "\r\n"
                + "Signed exposure: " + signedExposure();
    }
}
