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
        orderToChange = orderToChangeAmount;
        callable = initCallable(() -> orderToChangeAmount.setRequestedAmount(newAmount), orderToChangeAmount);
        callReason = OrderCallReason.CHANGE_AMOUNT;
        currentValue = orderToChangeAmount.getRequestedAmount();
        newValue = newAmount;
        valueName = "amount";
        createCommonLog();
    }

    @Override
    protected void initAttributes() {
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
}
