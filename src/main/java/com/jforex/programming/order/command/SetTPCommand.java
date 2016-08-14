package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Set;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public final class SetTPCommand extends OrderChangeCommand<Double> {

    private Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_TP;
    private static final ImmutableSet<OrderEventType> doneEventTypes =
            Sets.immutableEnumSet(CHANGED_TP);
    private static final ImmutableSet<OrderEventType> rejectEventTypes =
            Sets.immutableEnumSet(CHANGE_TP_REJECTED);
    private static final ImmutableSet<OrderEventType> infoEventTypes =
            Sets.immutableEnumSet(NOTIFICATION);
    private static final ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(Sets.union(infoEventTypes, Sets.union(doneEventTypes, rejectEventTypes)));

    public SetTPCommand(final IOrder orderToChangeTP,
                        final double newTP) {
        orderToChange = orderToChangeTP;
        callable = () -> {
            orderToChangeTP.setTakeProfitPrice(newTP);
            return orderToChangeTP;
        };
        currentValue = orderToChangeTP.getTakeProfitPrice();
        newValue = newTP;
        valueName = "TP";
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
        return !isTPSetTo(newValue).test(orderToChange);
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
