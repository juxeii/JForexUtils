package com.jforex.programming.position;

import static com.jforex.programming.order.event.OrderEventTypeSets.createEvents;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public class PositionFactory {

    private final Observable<OrderEvent> orderEventObservable;
    private final Map<Instrument, Position> positionByInstrument = new ConcurrentHashMap<>();

    public PositionFactory(final Observable<OrderEvent> orderEventObservable) {
        this.orderEventObservable = orderEventObservable;
    }

    public Collection<Position> allPositions() {
        return positionByInstrument.values();
    }

    public void addOrderOfEventToPosition(final OrderEvent orderEvent) {
        if (createEvents.contains(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            forInstrument(order.getInstrument()).addOrder(order);
        }
    }

    public Position forInstrument(final Instrument instrument) {
        return positionByInstrument.computeIfAbsent(instrument, this::createNew);
    }

    private final Position createNew(final Instrument instrument) {
        return new Position(instrument, orderEventObservable);
    }
}
