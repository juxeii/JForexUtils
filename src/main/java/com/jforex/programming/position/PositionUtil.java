package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;

public class PositionUtil {

    private final PositionFactory positionFactory;

    public PositionUtil(final PositionFactory positionFactory) {
        this.positionFactory = positionFactory;
    }

    public final PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(checkNotNull(instrument));
    }
}
