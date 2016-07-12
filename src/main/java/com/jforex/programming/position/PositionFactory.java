package com.jforex.programming.position;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public class PositionFactory {

    private final Observable<OrderEvent> orderEventObservable;
    private final Map<Instrument, Position> positionByInstrument = new ConcurrentHashMap<>();

    public PositionFactory(final Observable<OrderEvent> orderEventObservable) {
        this.orderEventObservable = orderEventObservable;
    }

    public Position createNew(final Instrument instrument) {
        return new Position(instrument, orderEventObservable);
    }

    public Position forInstrument(final Instrument instrument) {
        return positionByInstrument.computeIfAbsent(instrument, this::createNew);
    }

    public Collection<Position> all() {
        return positionByInstrument.values();
    }
}
