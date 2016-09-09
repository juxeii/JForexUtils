package com.jforex.programming.position;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderUtilImpl;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public class PositionFactory {

    private final Observable<OrderEvent> orderEventObservable;
    private final Map<Instrument, Position> positionByInstrument = new ConcurrentHashMap<>();

    public PositionFactory(final Observable<OrderEvent> orderEventObservable) {
        this.orderEventObservable = orderEventObservable;
    }

    public PositionUtil positionUtil(final OrderUtilImpl orderUtilImpl,
                                     final PositionFactory positionFactory) {
        return new PositionUtil(orderUtilImpl, positionFactory);
    }

    public Collection<Position> allPositions() {
        return positionByInstrument.values();
    }

    public Position forInstrument(final Instrument instrument) {
        return positionByInstrument.computeIfAbsent(instrument, this::createNew);
    }

    private final Position createNew(final Instrument instrument) {
        return new Position(instrument, orderEventObservable);
    }
}
