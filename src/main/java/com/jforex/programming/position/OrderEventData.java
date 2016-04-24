package com.jforex.programming.position;

import static com.jforex.programming.order.event.OrderEventType.AMOUNT_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_OPENPRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.GTT_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.LABEL_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.PRICE_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.SL_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.TP_CHANGE_OK;

import java.util.EnumSet;

import com.jforex.programming.order.event.OrderEventType;

public final class OrderEventData {

    private final EnumSet<OrderEventType> doneEvents;
    private final EnumSet<OrderEventType> rejectEvents;

    private OrderEventData(final EnumSet<OrderEventType> doneEvents,
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

    public final static OrderEventData submitEvents = new OrderEventData(EnumSet.of(FULL_FILL_OK),
                                                                         EnumSet.of(FILL_REJECTED, SUBMIT_REJECTED));

    public final static OrderEventData mergeEvents = new OrderEventData(EnumSet.of(MERGE_OK, MERGE_CLOSE_OK),
                                                                        EnumSet.of(MERGE_REJECTED));

    public final static OrderEventData closeEvents = new OrderEventData(EnumSet.of(CLOSE_OK),
                                                                        EnumSet.of(CLOSE_REJECTED));

    public final static OrderEventData changeLabelEvents = new OrderEventData(EnumSet.of(LABEL_CHANGE_OK),
                                                                              EnumSet.of(CHANGE_LABEL_REJECTED));

    public final static OrderEventData changeGTTEvents = new OrderEventData(EnumSet.of(GTT_CHANGE_OK),
                                                                            EnumSet.of(CHANGE_GTT_REJECTED));

    public final static OrderEventData changeOpenPriceEvents = new OrderEventData(EnumSet.of(PRICE_CHANGE_OK),
                                                                                  EnumSet.of(CHANGE_OPENPRICE_REJECTED));

    public final static OrderEventData changeAmountEvents = new OrderEventData(EnumSet.of(AMOUNT_CHANGE_OK),
                                                                               EnumSet.of(CHANGE_AMOUNT_REJECTED));

    public final static OrderEventData changeSLEvents = new OrderEventData(EnumSet.of(SL_CHANGE_OK),
                                                                           EnumSet.of(CHANGE_SL_REJECTED));

    public final static OrderEventData changeTPEvents = new OrderEventData(EnumSet.of(TP_CHANGE_OK),
                                                                           EnumSet.of(CHANGE_TP_REJECTED));
}
