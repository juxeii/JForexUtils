package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeLabelEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetLabelCommand implements OrderChangeCommand<String> {

    private final IOrder orderToChangeLabel;
    private final String currentValue;
    private final String newValue;
    private final String valueName;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_LABEL;
    private static final OrderEventTypeData orderEventTypeData = changeLabelEventTypeData;

    public SetLabelCommand(final IOrder orderToChangeLabel,
                           final String newLabel) {
        this.orderToChangeLabel = orderToChangeLabel;
        callable = () -> {
            orderToChangeLabel.setLabel(newLabel);
            return orderToChangeLabel;
        };
        currentValue = orderToChangeLabel.getLabel();
        newValue = newLabel;
        valueName = "label";
    }

    @Override
    public final OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    @Override
    public final Callable<IOrder> callable() {
        return callable;
    }

    @Override
    public final OrderCallReason callReason() {
        return callReason;
    }

    @Override
    public final boolean filter() {
        return !isLabelSetTo(newValue).test(orderToChangeLabel);
    }

    @Override
    public final IOrder order() {
        return orderToChangeLabel;
    }

    @Override
    public final String currentValue() {
        return currentValue;
    }

    @Override
    public final String newValue() {
        return newValue;
    }

    @Override
    public final String valueName() {
        return valueName;
    }
}
