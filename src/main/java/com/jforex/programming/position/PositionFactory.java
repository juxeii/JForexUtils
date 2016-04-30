package com.jforex.programming.position;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.order.OrderUtilObservable;

import com.dukascopy.api.Instrument;

public final class PositionFactory {

    private final OrderUtilObservable orderUtilObservable;
    private final ConcurrentUtil concurrentUtil;
    private final Map<Instrument, Position> positionByInstrument = new ConcurrentHashMap<>();

    public PositionFactory(final OrderUtilObservable orderUtilObservable,
                              final ConcurrentUtil concurrentUtil) {
        this.orderUtilObservable = orderUtilObservable;
        this.concurrentUtil = concurrentUtil;
    }

    public final Position createNew(final Instrument instrument,
                                    final RestoreSLTPPolicy restoreSLTPPolicy) {
        return new Position(instrument,
                            orderUtilObservable,
                            restoreSLTPPolicy,
                            concurrentUtil);
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
