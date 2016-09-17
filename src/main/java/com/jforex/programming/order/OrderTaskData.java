package com.jforex.programming.order;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class OrderTaskData {

    private final IOrder order;
    private final OrderCallReason callReason;
    private final OrderEventTypeData typeData;

    public OrderTaskData(final IOrder order,
                         final OrderCallReason callReason,
                         final OrderEventTypeData typeData) {
        this.order = order;
        this.callReason = callReason;
        this.typeData = typeData;
    }

    public IOrder order() {
        return order;
    }

    public OrderCallReason callReason() {
        return callReason;
    }

    public boolean isEventTypeForTask(final OrderEventType orderEventType) {
        return typeData
            .allEventTypes()
            .contains(orderEventType);
    }

    public boolean isRejectEventType(final OrderEventType orderEventType) {
        return typeData
            .rejectEventTypes()
            .contains(orderEventType);
    }

    public boolean isFinishEventType(final OrderEventType orderEventType) {
        return isDoneEventType(orderEventType) || isRejectEventType(orderEventType);
    }

    public boolean isDoneEventType(final OrderEventType orderEventType) {
        return typeData
            .doneEventTypes()
            .contains(orderEventType);
    }
}
