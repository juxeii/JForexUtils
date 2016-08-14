package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;

public final class SetLabelCommand extends OrderChangeCommand<String> {

    public SetLabelCommand(final IOrder orderToChangeLabel,
                           final String newLabel) {
        orderToChange = orderToChangeLabel;
        callable = initCallable(() -> orderToChangeLabel.setLabel(newLabel), orderToChangeLabel);
        callReason = OrderCallReason.CHANGE_LABEL;
        currentValue = orderToChangeLabel.getLabel();
        newValue = newLabel;
        valueName = "label";
        createCommonLog();
    }

    @Override
    protected void initAttributes() {
        doneEventTypes =
                Sets.immutableEnumSet(CHANGED_LABEL);
        rejectEventTypes =
                Sets.immutableEnumSet(CHANGE_LABEL_REJECTED);
        infoEventTypes =
                Sets.immutableEnumSet(NOTIFICATION);
    }

    @Override
    public final boolean filter() {
        return !isLabelSetTo(newValue).test(orderToChange);
    }
}
