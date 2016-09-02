package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;
import static com.jforex.programming.order.event.OrderEventTypeSets.createEvents;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;

public class PositionUtil {

    private final PositionFactory positionFactory;

    public PositionUtil(final PositionFactory positionFactory) {
        this.positionFactory = positionFactory;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public Position position(final Collection<IOrder> orders) {
        return position(instrumentFromOrders(orders));
    }

    private Position position(final Instrument instrument) {
        return (Position) positionOrders(instrument);
    }

    public void addOrderToPosition(final OrderEvent orderEvent) {
        if (createEvents.contains(orderEvent.type())) {
            final IOrder order = orderEvent.order();
            position(order.getInstrument()).addOrder(order);
        }
    }
}
