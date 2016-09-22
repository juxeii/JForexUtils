package com.jforex.programming.position;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

public class PositionUtil {

    private final PositionFactory positionFactory;

    public PositionUtil(final PositionFactory positionFactory) {
        this.positionFactory = positionFactory;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Collection<IOrder> filledOrders(final Instrument instrument) {
        return positionOrders(instrument).filled();
    }

    public Collection<IOrder> openedOrders(final Instrument instrument) {
        return positionOrders(instrument).opened();
    }

    public Collection<IOrder> filledOrOpenedOrders(final Instrument instrument) {
        return positionOrders(instrument).filledOrOpened();
    }
}
