package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isOpenPriceSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Set;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public final class SetOpenPriceCommand extends OrderChangeCommand<Double> {

    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_PRICE;
    private static final ImmutableSet<OrderEventType> doneEventTypes =
            Sets.immutableEnumSet(CHANGED_PRICE);
    private static final ImmutableSet<OrderEventType> rejectEventTypes =
            Sets.immutableEnumSet(CHANGE_PRICE_REJECTED);
    private static final ImmutableSet<OrderEventType> infoEventTypes =
            Sets.immutableEnumSet(NOTIFICATION);
    private static final ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(Sets.union(infoEventTypes, Sets.union(doneEventTypes, rejectEventTypes)));

    public SetOpenPriceCommand(final IOrder orderToChangeOpenPrice,
                               final double newOpenPrice) {
        orderToChange = orderToChangeOpenPrice;
        callable = () -> {
            orderToChangeOpenPrice.setOpenPrice(newOpenPrice);
            return orderToChangeOpenPrice;
        };
        currentValue = orderToChangeOpenPrice.getOpenPrice();
        newValue = newOpenPrice;
        valueName = "open price";
        createCommonLog();
    }

    @Override
    public Set<OrderEventType> allEventTypes() {
        return allEventTypes;
    }

    @Override
    public Set<OrderEventType> doneEventTypes() {
        return doneEventTypes;
    }

    @Override
    public Set<OrderEventType> rejectEventTypes() {
        return rejectEventTypes;
    }

    @Override
    public Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public OrderCallReason callReason() {
        return callReason;
    }

    @Override
    public final boolean filter() {
        return !isOpenPriceSetTo(newValue).test(orderToChange);
    }
}
