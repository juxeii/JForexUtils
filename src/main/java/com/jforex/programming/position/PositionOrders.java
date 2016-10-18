package com.jforex.programming.position;

import static com.jforex.programming.order.OrderStaticUtil.isFilled;
import static com.jforex.programming.order.OrderStaticUtil.isOpened;

import java.util.Set;
import java.util.function.Predicate;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.PositionDirection;
import com.jforex.programming.order.OrderStaticUtil;

public interface PositionOrders {

    public Instrument instrument();

    public Set<IOrder> all();

    public int size();

    public boolean contains(IOrder order);

    public Set<IOrder> filter(Predicate<IOrder> orderPredicate);

    default PositionDirection direction() {
        return OrderStaticUtil.positionDirection(filter(isFilled));
    }

    default double signedExposure() {
        return filter(isFilled)
            .stream()
            .mapToDouble(OrderStaticUtil::signedAmount)
            .sum();
    }

    default Set<IOrder> filled() {
        return filter(isFilled);
    }

    default Set<IOrder> opened() {
        return filter(isOpened);
    }

    default Set<IOrder> filledOrOpened() {
        return filter(isFilled.or(isOpened));
    }
}
