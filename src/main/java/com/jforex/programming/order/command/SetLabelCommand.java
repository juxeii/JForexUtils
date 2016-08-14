package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.event.OrderEventTypeData.changeLabelEventTypeData;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetLabelCommand implements OrderChangeCommand<String> {

    private final IOrder orderToChangeLabel;
    private final String newLabel;
    private final Callable<IOrder> callable;

    private static final OrderCallReason callReason = OrderCallReason.CHANGE_LABEL;
    private static final OrderEventTypeData orderEventTypeData = changeLabelEventTypeData;

    public SetLabelCommand(final IOrder orderToChangeLabel,
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
    public final boolean filter() {
        return !isLabelSetTo(newLabel).test(orderToChangeLabel);
    }

    @Override
    public final OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    @Override
    public final OrderCallReason callReason() {
        return callReason;
    }
}
