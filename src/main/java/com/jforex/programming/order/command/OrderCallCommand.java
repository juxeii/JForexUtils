package com.jforex.programming.order.command;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class OrderCallCommand {

    private final CommandData commandData;
    private final OrderEventTypeData orderEventTypeData;

    public OrderCallCommand(final CommandData commandData) {
        this.commandData = commandData;
        orderEventTypeData = commandData.orderEventTypeData();
    }

    public final Callable<IOrder> callable() {
        return commandData.callable();
    }

    public final OrderCallReason callReason() {
        return commandData.callReason();
    }

    public final boolean isEventForCommand(final OrderEvent orderEvent) {
        return orderEventTypeData
            .allEventTypes()
            .contains(orderEvent.type());
    }

    public final boolean isDoneEvent(final OrderEvent orderEvent) {
        return orderEventTypeData
            .doneEventTypes()
            .contains(orderEvent.type());
    }

    public final boolean isRejectEvent(final OrderEvent orderEvent) {
        return orderEventTypeData
            .rejectEventTypes()
            .contains(orderEvent.type());
    }
}
