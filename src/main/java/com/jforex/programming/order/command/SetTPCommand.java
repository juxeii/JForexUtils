package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isTPSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;

public final class SetTPCommand extends OrderChangeCommand<Double> {

    public SetTPCommand(final IOrder orderToChangeTP,
                        final double newTP) {
        orderToChange = orderToChangeTP;
        callable = initCallable(() -> orderToChangeTP.setTakeProfitPrice(newTP), orderToChangeTP);
        callReason = OrderCallReason.CHANGE_TP;
        currentValue = orderToChangeTP.getTakeProfitPrice();
        newValue = newTP;
        valueName = "TP";
        createCommonLog();
    }

    @Override
    protected void initAttributes() {
        doneEventTypes =
                Sets.immutableEnumSet(CHANGED_TP);
        rejectEventTypes =
                Sets.immutableEnumSet(CHANGE_TP_REJECTED);
        infoEventTypes =
                Sets.immutableEnumSet(NOTIFICATION);
    }

    @Override
    public final boolean filter() {
        return !isTPSetTo(newValue).test(orderToChange);
    }
}
