package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetLabelCommandData implements OrderChangeCommandData<String> {

    private final IOrder orderToChangeLabel;
    private final String newLabel;
    private final Callable<IOrder> callable;

    private static final OrderEventTypeData orderEventTypeData =
            new OrderEventTypeData(EnumSet.of(CHANGED_LABEL),
                                   EnumSet.of(CHANGE_LABEL_REJECTED),
                                   EnumSet.of(NOTIFICATION));

    public SetLabelCommandData(final IOrder orderToChangeLabel,
                               final String newLabel) {
        this.orderToChangeLabel = orderToChangeLabel;
        this.newLabel = newLabel;
        callable = () -> {
            orderToChangeLabel.setLabel(newLabel);
            return orderToChangeLabel;
        };
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final boolean isValueNotSet() {
        return !isLabelSetTo(newLabel).test(orderToChangeLabel);
    }

    @Override
    public final OrderCallReason callReason() {
        return OrderCallReason.CHANGE_LABEL;
    }

    @Override
    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }
}
