package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;

public final class SetSLCommand extends OrderChangeCommand<Double> {

    public SetSLCommand(final IOrder orderToChangeSL,
                        final double newSL) {
        super(orderToChangeSL,
              () -> orderToChangeSL.setStopLossPrice(newSL),
              orderToChangeSL.getStopLossPrice(),
              newSL,
              "SL");
    }

    @Override
    protected void initEventTypes() {
        doneEventTypes =
                Sets.immutableEnumSet(CHANGED_SL);
        rejectEventTypes =
                Sets.immutableEnumSet(CHANGE_SL_REJECTED);
        infoEventTypes =
                Sets.immutableEnumSet(NOTIFICATION);
    }

    @Override
    public final boolean filter() {
        return !isSLSetTo(newValue).test(orderToChange);
    }

    @Override
    public OrderCallReason callReason() {
        return OrderCallReason.CHANGE_SL;
    }
}
