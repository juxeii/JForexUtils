package com.jforex.programming.position;

import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.SL_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.TP_CHANGE_OK;

import java.util.EnumSet;

import com.jforex.programming.order.event.OrderEventType;

public final class TaskEventData {

    private final EnumSet<OrderEventType> doneEvents;
    private final EnumSet<OrderEventType> rejectEvents;

    private TaskEventData(final EnumSet<OrderEventType> doneEvents,
                          final EnumSet<OrderEventType> rejectEvents) {
        this.rejectEvents = rejectEvents;
        this.doneEvents = doneEvents;
    }

    public final EnumSet<OrderEventType> forDone() {
        return doneEvents;
    }

    public final EnumSet<OrderEventType> forReject() {
        return rejectEvents;
    }

    public final EnumSet<OrderEventType> all() {
        final EnumSet<OrderEventType> allEvents = EnumSet.copyOf(doneEvents);
        allEvents.addAll(rejectEvents);
        return allEvents;
    }

    public final static TaskEventData submitEvents = new TaskEventData(EnumSet.of(FULL_FILL_OK),
                                                                       EnumSet.of(FILL_REJECTED, SUBMIT_REJECTED));

    public final static TaskEventData mergeEvents = new TaskEventData(EnumSet.of(MERGE_OK, MERGE_CLOSE_OK),
                                                                      EnumSet.of(MERGE_REJECTED));

    public final static TaskEventData closeEvents = new TaskEventData(EnumSet.of(CLOSE_OK),
                                                                      EnumSet.of(CLOSE_REJECTED));

    public final static TaskEventData changeSLEvents = new TaskEventData(EnumSet.of(SL_CHANGE_OK),
                                                                         EnumSet.of(CHANGE_SL_REJECTED));

    public final static TaskEventData changeTPEvents = new TaskEventData(EnumSet.of(TP_CHANGE_OK),
                                                                         EnumSet.of(CHANGE_TP_REJECTED));
}
