package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;

import java.util.Set;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public final class CloseCommand extends OrderChangeCommand<IOrder.State> {

    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CLOSE;
    private static final ImmutableSet<OrderEventType> doneEventTypes =
            Sets.immutableEnumSet(CLOSE_OK);
    private static final ImmutableSet<OrderEventType> rejectEventTypes =
            Sets.immutableEnumSet(CLOSE_REJECTED);
    private static final ImmutableSet<OrderEventType> infoEventTypes =
            Sets.immutableEnumSet(NOTIFICATION, PARTIAL_CLOSE_OK);
    private static final ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(Sets.union(infoEventTypes, Sets.union(doneEventTypes, rejectEventTypes)));

    public CloseCommand(final IOrder orderToClose) {
        orderToChange = orderToClose;
        callable = () -> {
            orderToClose.close();
            return orderToClose;
        };
        currentValue = orderToClose.getState();
        newValue = IOrder.State.CLOSED;
        valueName = "open price";
        createCommonLog();
    }

    @Override
    public final boolean filter() {
        return !isClosed.test(orderToChange);
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
}
