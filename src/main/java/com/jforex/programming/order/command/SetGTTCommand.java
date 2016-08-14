package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Set;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public final class SetGTTCommand extends OrderChangeCommand<Long> {

    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_GTT;
    private static final ImmutableSet<OrderEventType> doneEventTypes =
            Sets.immutableEnumSet(CHANGED_GTT);
    private static final ImmutableSet<OrderEventType> rejectEventTypes =
            Sets.immutableEnumSet(CHANGE_GTT_REJECTED);
    private static final ImmutableSet<OrderEventType> infoEventTypes =
            Sets.immutableEnumSet(NOTIFICATION);
    private static final ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(Sets.union(infoEventTypes, Sets.union(doneEventTypes, rejectEventTypes)));

    public SetGTTCommand(final IOrder orderToChangeGTT,
                         final long newGTT) {
        orderToChange = orderToChangeGTT;
        callable = () -> {
            orderToChangeGTT.setGoodTillTime(newGTT);
            return orderToChangeGTT;
        };
        currentValue = orderToChangeGTT.getGoodTillTime();
        newValue = newGTT;
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
    public final boolean filter() {
        return !isGTTSetTo(newValue).test(orderToChange);
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
