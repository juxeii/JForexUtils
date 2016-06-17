package com.jforex.programming.order.event;

import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_OPENPRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CLOSED_BY_MERGE;
import static com.jforex.programming.order.event.OrderEventType.CLOSED_BY_SL;
import static com.jforex.programming.order.event.OrderEventType.CLOSED_BY_TP;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public final class OrderEventTypeSets {

    private OrderEventTypeSets() {
    }

    public final static ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(EnumSet.allOf(OrderEventType.class));

    public final static ImmutableSet<OrderEventType> intermediateEventTypes =
            Sets.immutableEnumSet(SUBMIT_OK,
                                  PARTIAL_FILL_OK,
                                  PARTIAL_CLOSE_OK);

    public final static ImmutableSet<OrderEventType> closeEventTypes =
            Sets.immutableEnumSet(CLOSE_OK,
                                  CLOSED_BY_MERGE,
                                  CLOSED_BY_SL,
                                  CLOSED_BY_TP,
                                  MERGE_CLOSE_OK);

    public final static ImmutableSet<OrderEventType> endOfOrderEventTypes =
            Sets.immutableEnumSet(Sets.union(closeEventTypes,
                                             EnumSet.of(SUBMIT_REJECTED,
                                                        FILL_REJECTED,
                                                        MERGE_REJECTED)));

    public final static ImmutableSet<OrderEventType> notEndOfOrderEventTypes =
            Sets.immutableEnumSet(Sets.complementOf(endOfOrderEventTypes));

    public final static Set<OrderEventType> finishEventTypes =
            Sets.immutableEnumSet(Sets.union(endOfOrderEventTypes,
                                             EnumSet.of(FULLY_FILLED,
                                                        CHANGED_AMOUNT,
                                                        CHANGED_GTT,
                                                        CHANGED_LABEL,
                                                        CHANGED_SL,
                                                        CHANGED_TP,
                                                        CHANGED_PRICE,
                                                        MERGE_OK,
                                                        CHANGED_REJECTED,
                                                        CHANGE_AMOUNT_REJECTED,
                                                        CHANGE_GTT_REJECTED,
                                                        CHANGE_OPENPRICE_REJECTED,
                                                        CHANGE_TP_REJECTED,
                                                        CHANGE_SL_REJECTED,
                                                        CHANGE_LABEL_REJECTED)));
}
