package com.jforex.programming.order.event;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CLOSED_BY_MERGE;
import static com.jforex.programming.order.event.OrderEventType.CLOSED_BY_SL;
import static com.jforex.programming.order.event.OrderEventType.CLOSED_BY_TP;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public final class OrderEventTypeSets {

    private OrderEventTypeSets() {
    }

    public static final ImmutableSet<OrderEventType> allEventTypes =
            Sets.immutableEnumSet(EnumSet.allOf(OrderEventType.class));

    public static final ImmutableSet<OrderEventType> doneEventTypes =
            Sets.immutableEnumSet(EnumSet.of(FULLY_FILLED,
                                             SUBMIT_CONDITIONAL_OK,
                                             CHANGED_AMOUNT,
                                             CHANGED_GTT,
                                             CHANGED_LABEL,
                                             CHANGED_SL,
                                             CHANGED_TP,
                                             CHANGED_PRICE,
                                             MERGE_OK,
                                             CLOSE_OK,
                                             CLOSED_BY_MERGE,
                                             CLOSED_BY_SL,
                                             CLOSED_BY_TP,
                                             MERGE_CLOSE_OK));

    public static final ImmutableSet<OrderEventType> rejectEventTypes =
            Sets.immutableEnumSet(SUBMIT_REJECTED,
                                  FILL_REJECTED,
                                  MERGE_REJECTED,
                                  CLOSE_REJECTED,
                                  CHANGED_REJECTED,
                                  CHANGE_SL_REJECTED,
                                  CHANGE_TP_REJECTED,
                                  CHANGE_GTT_REJECTED,
                                  CHANGE_LABEL_REJECTED,
                                  CHANGE_AMOUNT_REJECTED,
                                  CHANGE_PRICE_REJECTED);

    public static final Set<OrderEventType> createEventTypes =
            Sets.immutableEnumSet(EnumSet.of(FULLY_FILLED,
                                             SUBMIT_CONDITIONAL_OK,
                                             MERGE_OK));

    public static final Set<OrderEventType> finishEventTypes =
            Sets.immutableEnumSet(Sets.union(doneEventTypes, rejectEventTypes));
}
