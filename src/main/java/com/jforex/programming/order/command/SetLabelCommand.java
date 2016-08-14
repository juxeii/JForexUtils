package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Set;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public final class SetLabelCommand extends OrderChangeCommand<String> {

    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_LABEL;
    private static final ImmutableSet<OrderEventType> doneEventTypes =
            Sets.immutableEnumSet(CHANGED_LABEL);
    private static final ImmutableSet<OrderEventType> rejectEventTypes =
            Sets.immutableEnumSet(CHANGE_LABEL_REJECTED);
    private static final ImmutableSet<OrderEventType> infoEventTypes =
            Sets.immutableEnumSet(NOTIFICATION);
    private static final ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(Sets.union(infoEventTypes, Sets.union(doneEventTypes, rejectEventTypes)));

    public SetLabelCommand(final IOrder orderToChangeLabel,
                           final String newLabel) {
        orderToChange = orderToChangeLabel;
        callable = () -> {
            orderToChangeLabel.setLabel(newLabel);
            return orderToChangeLabel;
        };
        currentValue = orderToChangeLabel.getLabel();
        newValue = newLabel;
        valueName = "label";
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
        return !isLabelSetTo(newValue).test(orderToChange);
    }
}
