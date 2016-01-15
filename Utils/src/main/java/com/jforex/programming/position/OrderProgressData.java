package com.jforex.programming.position;

import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public final class OrderProgressData {

    private final TaskEventData taskEventData;
    private final Consumer<OrderEvent> doneAction;
    private final Consumer<OrderEvent> rejectAction;

    public OrderProgressData(final TaskEventData taskEventData,
                             final Consumer<OrderEvent> doneAction,
                             final Consumer<OrderEvent> rejectAction) {
        this.taskEventData = taskEventData;
        this.doneAction = doneAction;
        this.rejectAction = rejectAction;
    }

    public final TaskEventData taskEventData() {
        return taskEventData;
    }

    public final void processOrderEventType(final OrderEvent orderEvent) {
        final OrderEventType orderEventType = orderEvent.type();
        if (taskEventData.forReject().contains(orderEventType))
            rejectAction.accept(orderEvent);
        else if (taskEventData.forDone().contains(orderEventType))
            doneAction.accept(orderEvent);
    }
}
