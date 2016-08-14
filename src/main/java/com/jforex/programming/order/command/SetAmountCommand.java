package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetAmountCommand extends OrderChangeCommand<Double> {

    public SetAmountCommand(final IOrder orderToChangeAmount,
                            final double newAmount) {
        super(orderToChangeAmount,
              () -> orderToChangeAmount.setRequestedAmount(newAmount),
              OrderEventTypeData.changeAmountData,
              orderToChangeAmount.getRequestedAmount(),
              newAmount,
              "amount");
    }

    @Override
    public final boolean filter() {
        return !isAmountSetTo(newValue).test(orderToChange);
    }

    @Override
    public OrderCallReason callReason() {
        return OrderCallReason.CHANGE_AMOUNT;
    }

    @Override
    protected void initDoneEvents() {
        doneEventTypes = Sets.immutableEnumSet(CHANGED_AMOUNT);
    }

    @Override
    protected void initRejectEvents() {
        rejectEventTypes = Sets.immutableEnumSet(CHANGE_AMOUNT_REJECTED);
    }

    @Override
    protected void initInfoEvents() {
        infoEventTypes = Sets.immutableEnumSet(NOTIFICATION);
    }
}
