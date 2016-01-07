package com.jforex.programming.position;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderRepository;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public class PositionRepository {

    private final OrderUtil orderUtil;
    private final Observable<OrderEvent> orderEventObservable;
    private final Map<Instrument, Position> positionByInstrument = new ConcurrentHashMap<>();

    public PositionRepository(final OrderUtil orderUtil,
                              final Observable<OrderEvent> orderEventObservable) {
        this.orderUtil = orderUtil;
        this.orderEventObservable = orderEventObservable;
    }

    public Position createNew(final Instrument instrument,
                              final RestoreSLTPPolicy restoreSLTPPolicy) {
        final OrderRepository orderRepository = new OrderRepository();
        return new Position(instrument,
                            orderUtil,
                            orderRepository,
                            orderEventObservable,
                            restoreSLTPPolicy);
    }

    public Position forInstrument(final Instrument instrument,
                                  final RestoreSLTPPolicy restoreSLTPPolicy) {
        positionByInstrument.computeIfAbsent(instrument, i -> createNew(i, restoreSLTPPolicy));
        return positionByInstrument.get(instrument);
    }

    public Collection<Position> all() {
        return positionByInstrument.values();
    }
}
