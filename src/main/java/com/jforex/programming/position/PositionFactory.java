package com.jforex.programming.position;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public final class PositionFactory {

    private final PositionTask positionTask;
    private final Observable<OrderEvent> orderEventObservable;
    private final Map<Instrument, Position> positionByInstrument = new ConcurrentHashMap<>();

    public PositionFactory(final PositionTask positionTask,
                           final Observable<OrderEvent> orderEventObservable) {
        this.positionTask = positionTask;
        this.orderEventObservable = orderEventObservable;
    }

    public final Position createNew(final Instrument instrument,
                                    final RestoreSLTPPolicy restoreSLTPPolicy) {
        return new Position(instrument,
                            positionTask,
                            orderEventObservable,
                            restoreSLTPPolicy);
    }

    public final Position forInstrument(final Instrument instrument,
                                        final RestoreSLTPPolicy restoreSLTPPolicy) {
        return positionByInstrument.computeIfAbsent(instrument,
                                                    inst -> createNew(inst, restoreSLTPPolicy));
    }

    public final Collection<Position> all() {
        return positionByInstrument.values();
    }
}
