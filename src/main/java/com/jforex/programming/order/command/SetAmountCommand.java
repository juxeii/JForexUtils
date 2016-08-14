package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isAmountSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;

public final class SetAmountCommand extends OrderChangeCommand<Double> {

    public SetAmountCommand(final IOrder orderToChangeAmount,
                            final double newAmount) {
        super(orderToChangeAmount,
              () -> orderToChangeAmount.setRequestedAmount(newAmount),
              orderToChangeAmount.getRequestedAmount(),
              newAmount,
              "amount");
    }

    @Override
    protected void initEventTypes() {
        doneEventTypes =
                Sets.immutableEnumSet(CHANGED_AMOUNT);
        rejectEventTypes =
                Sets.immutableEnumSet(CHANGE_AMOUNT_REJECTED);
        infoEventTypes =
                Sets.immutableEnumSet(NOTIFICATION);
    }

    @Override
    public final boolean filter() {
        return !isAmountSetTo(newValue).test(orderToChange);
    }

    @Override
    public OrderCallReason callReason() {
        return OrderCallReason.CHANGE_AMOUNT;
    }
}
