package com.jforex.programming.order.event;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;

import java.util.EnumSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public final class OrderEventTypeSets {

    private OrderEventTypeSets() {
    }

    public static final ImmutableSet<OrderEventType> allEvents =
            Sets.immutableEnumSet(EnumSet.allOf(OrderEventType.class));

    public static final ImmutableSet<OrderEventType> rejectEvents =
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

    public static final ImmutableSet<OrderEventType> infoEvents =
            Sets.immutableEnumSet(NOTIFICATION,
                                  PARTIAL_FILL_OK,
                                  PARTIAL_CLOSE_OK);

    public static final ImmutableSet<OrderEventType> createEvents =
            Sets.immutableEnumSet(SUBMIT_OK, MERGE_OK);
}
