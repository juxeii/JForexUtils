package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Set;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public final class SetAmountCommand extends OrderChangeCommand<Double> {

    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_AMOUNT;
    private static final ImmutableSet<OrderEventType> doneEventTypes =
            Sets.immutableEnumSet(CHANGED_AMOUNT);
    private static final ImmutableSet<OrderEventType> rejectEventTypes =
            Sets.immutableEnumSet(CHANGE_AMOUNT_REJECTED);
    private static final ImmutableSet<OrderEventType> infoEventTypes =
            Sets.immutableEnumSet(NOTIFICATION);
    private static final ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(Sets.union(infoEventTypes, Sets.union(doneEventTypes, rejectEventTypes)));

    public SetAmountCommand(final IOrder orderToChangeAmount,
                            final double newAmount) {
        orderToChange = orderToChangeAmount;
        callable = () -> {
            orderToChangeAmount.setRequestedAmount(newAmount);
            return orderToChangeAmount;
        };
        currentValue = orderToChangeAmount.getRequestedAmount();
        newValue = newAmount;
        valueName = "amount";
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
        return !isAmountSetTo(newValue).test(orderToChange);
    }
}
