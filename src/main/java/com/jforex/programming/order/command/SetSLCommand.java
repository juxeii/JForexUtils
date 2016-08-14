package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Set;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public final class SetSLCommand extends OrderChangeCommand<Double> {

    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_SL;
    private static final ImmutableSet<OrderEventType> doneEventTypes =
            Sets.immutableEnumSet(CHANGED_SL);
    private static final ImmutableSet<OrderEventType> rejectEventTypes =
            Sets.immutableEnumSet(CHANGE_SL_REJECTED);
    private static final ImmutableSet<OrderEventType> infoEventTypes =
            Sets.immutableEnumSet(NOTIFICATION);
    private static final ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(Sets.union(infoEventTypes, Sets.union(doneEventTypes, rejectEventTypes)));

    public SetSLCommand(final IOrder orderToChangeSL,
                        final double newSL) {
        orderToChange = orderToChangeSL;
        callable = () -> {
            orderToChangeSL.setStopLossPrice(newSL);
            return orderToChangeSL;
        };
        currentValue = orderToChangeSL.getStopLossPrice();
        newValue = newSL;
        valueName = "SL";
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
    public final boolean filter() {
        return !isSLSetTo(newValue).test(orderToChange);
    }

    @Override
    public Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public OrderCallReason callReason() {
        return callReason;
    }
}
