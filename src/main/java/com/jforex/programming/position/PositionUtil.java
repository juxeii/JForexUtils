package com.jforex.programming.position;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;

public class PositionUtil {

    private final PositionFactory positionFactory;

    public PositionUtil(final PositionFactory positionFactory) {
        this.positionFactory = positionFactory;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return positionFactory.forInstrument(instrument);
    }

    public void create(final Instrument instrument) {
        positionFactory.forInstrument(instrument);
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

    public List<Observable<OrderEvent>> observablesFromFactory(final Function<Instrument,
                                                                              Observable<OrderEvent>> paramsFactory) {
        return Observable
            .fromIterable(positionFactory.all())
            .map(Position::instrument)
            .map(paramsFactory::apply)
            .toList()
            .blockingGet();
    }

    public static final PositionDirection direction(final Collection<IOrder> positionOrders) {
        checkNotNull(positionOrders);

        final double signedAmount = OrderStaticUtil.combinedSignedAmount(positionOrders);
        return directionForSignedAmount(signedAmount);
    }

    public static final PositionDirection directionForSignedAmount(final double signedAmount) {
        if (signedAmount > 0)
            return PositionDirection.LONG;
        return signedAmount < 0
                ? PositionDirection.SHORT
                : PositionDirection.FLAT;
    }
}
