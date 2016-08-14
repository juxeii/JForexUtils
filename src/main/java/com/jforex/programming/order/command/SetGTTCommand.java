package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;

public final class SetGTTCommand extends OrderChangeCommand<Long> {

    public SetGTTCommand(final IOrder orderToChangeGTT,
                         final long newGTT) {
        orderToChange = orderToChangeGTT;
        callable = initCallable(() -> orderToChangeGTT.setGoodTillTime(newGTT), orderToChangeGTT);
        callReason = OrderCallReason.CHANGE_GTT;
        currentValue = orderToChangeGTT.getGoodTillTime();
        newValue = newGTT;
        valueName = "open price";
        createCommonLog();
    }

    @Override
    protected void initAttributes() {
        doneEventTypes =
                Sets.immutableEnumSet(CHANGED_GTT);
        rejectEventTypes =
                Sets.immutableEnumSet(CHANGE_GTT_REJECTED);
        infoEventTypes =
                Sets.immutableEnumSet(NOTIFICATION);
    }

    @Override
    public final boolean filter() {
        return !isGTTSetTo(newValue).test(orderToChange);
    }
}
