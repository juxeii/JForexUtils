package com.jforex.programming.position;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jforex.programming.order.OrderUtilObservable;

import com.dukascopy.api.Instrument;

public final class PositionRepository {

    private final OrderUtilObservable orderUtilObservable;
    private final Map<Instrument, Position> positionByInstrument = new ConcurrentHashMap<>();

    public PositionRepository(final OrderUtilObservable orderUtilObservable) {
        this.orderUtilObservable = orderUtilObservable;
    }

    public final Position createNew(final Instrument instrument,
                                    final RestoreSLTPPolicy restoreSLTPPolicy) {
        return new Position(instrument,
                            orderUtilObservable,
                            restoreSLTPPolicy);
    }

    public final Position forInstrument(final Instrument instrument,
                                        final RestoreSLTPPolicy restoreSLTPPolicy) {
        positionByInstrument.computeIfAbsent(instrument, i -> createNew(i, restoreSLTPPolicy));
        return positionByInstrument.get(instrument);
    }

    public final Collection<Position> all() {
        return positionByInstrument.values();
    }
}
