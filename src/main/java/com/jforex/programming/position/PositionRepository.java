package com.jforex.programming.position;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public final class PositionRepository {

    private final OrderUtil orderUtil;
    private final Observable<OrderEvent> orderEventObservable;
    private final Map<Instrument, Position> positionByInstrument = new ConcurrentHashMap<>();

    public PositionRepository(final OrderUtil orderUtil,
                              final Observable<OrderEvent> orderEventObservable) {
        this.orderUtil = orderUtil;
        this.orderEventObservable = orderEventObservable;
    }

    public final Position createNew(final Instrument instrument,
                                    final RestoreSLTPPolicy restoreSLTPPolicy) {
        return new Position(instrument,
                            orderUtil,
                            orderEventObservable,
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
