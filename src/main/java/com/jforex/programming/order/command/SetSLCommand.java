package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isSLSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetSLCommand extends OrderChangeCommand<Double> {

    public SetSLCommand(final IOrder orderToChangeSL,
                        final double newSL) {
        super(orderToChangeSL,
              () -> orderToChangeSL.setStopLossPrice(newSL),
              OrderEventTypeData.changeSLData,
              orderToChangeSL.getStopLossPrice(),
              newSL,
              "SL");
    }

    @Override
    public final boolean filter() {
        return !isSLSetTo(newValue).test(orderToChange);
    }

    @Override
    public OrderCallReason callReason() {
        return OrderCallReason.CHANGE_SL;
    }

    @Override
    protected void initDoneEvents() {
        doneEventTypes = Sets.immutableEnumSet(CHANGED_SL);
    }

    @Override
    protected void initRejectEvents() {
        rejectEventTypes = Sets.immutableEnumSet(CHANGE_SL_REJECTED);
    }

    @Override
    protected void initInfoEvents() {
        infoEventTypes = Sets.immutableEnumSet(NOTIFICATION);
    }
}
