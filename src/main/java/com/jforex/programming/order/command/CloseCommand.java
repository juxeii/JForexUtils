package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isClosed;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;

public final class CloseCommand extends OrderChangeCommand<IOrder.State> {

    public CloseCommand(final IOrder orderToClose) {
        orderToChange = orderToClose;
        callable = initCallable(() -> orderToClose.close(), orderToClose);
        callReason = OrderCallReason.CLOSE;
        currentValue = orderToClose.getState();
        newValue = IOrder.State.CLOSED;
        valueName = "open price";
        createCommonLog();
    }

    @Override
    protected void initAttributes() {
        doneEventTypes =
                Sets.immutableEnumSet(CLOSE_OK);
        rejectEventTypes =
                Sets.immutableEnumSet(CLOSE_REJECTED);
        infoEventTypes =
                Sets.immutableEnumSet(NOTIFICATION, PARTIAL_CLOSE_OK);
    }

    @Override
    public final boolean filter() {
        return !isClosed.test(orderToChange);
    }
}
