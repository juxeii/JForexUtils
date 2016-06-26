package com.jforex.programming.order.event;

import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;

public final class OrderEventTypeData {

    private final ImmutableSet<OrderEventType> doneEventTypes;
    private final ImmutableSet<OrderEventType> rejectEventTypes;
    private final ImmutableSet<OrderEventType> allTypes;
    private final OrderCallReason callReason;

    private OrderEventTypeData(final EnumSet<OrderEventType> doneEventTypes,
                               final EnumSet<OrderEventType> rejectEventTypes,
                               final OrderCallReason callReason) {
        this.doneEventTypes = Sets.immutableEnumSet(doneEventTypes);
        this.rejectEventTypes = Sets.immutableEnumSet(rejectEventTypes);
        this.callReason = callReason;

        final EnumSet<OrderEventType> tmpAllTypes = doneEventTypes;
        tmpAllTypes.addAll(rejectEventTypes);
        allTypes = Sets.immutableEnumSet(tmpAllTypes);
    }

    public final boolean isDoneType(final OrderEventType orderEventType) {
        return doneEventTypes.contains(orderEventType);
    }

    public final boolean isRejectType(final OrderEventType orderEventType) {
        return rejectEventTypes.contains(orderEventType);
    }

    public final Set<OrderEventType> all() {
        return allTypes;
    }

    public final OrderCallReason callReason() {
        return callReason;
    }

    public final static OrderEventTypeData submitData =
            new OrderEventTypeData(EnumSet.of(FULLY_FILLED, SUBMIT_CONDITIONAL_OK, PARTIAL_FILL_OK),
                                   EnumSet.of(FILL_REJECTED, SUBMIT_REJECTED),
                                   OrderCallReason.SUBMIT);

    public final static OrderEventTypeData mergeData =
            new OrderEventTypeData(EnumSet.of(MERGE_OK, MERGE_CLOSE_OK),
                                   EnumSet.of(MERGE_REJECTED),
                                   OrderCallReason.MERGE);

    public final static OrderEventTypeData closeData =
            new OrderEventTypeData(EnumSet.of(CLOSE_OK),
                                   EnumSet.of(CLOSE_REJECTED),
                                   OrderCallReason.CLOSE);

    public final static OrderEventTypeData changeLabelData =
            new OrderEventTypeData(EnumSet.of(CHANGED_LABEL),
                                   EnumSet.of(CHANGE_LABEL_REJECTED),
                                   OrderCallReason.CHANGE_LABEL);

    public final static OrderEventTypeData changeGTTData =
            new OrderEventTypeData(EnumSet.of(CHANGED_GTT),
                                   EnumSet.of(CHANGE_GTT_REJECTED),
                                   OrderCallReason.CHANGE_GTT);

    public final static OrderEventTypeData changeOpenPriceData =
            new OrderEventTypeData(EnumSet.of(CHANGED_PRICE),
                                   EnumSet.of(CHANGE_PRICE_REJECTED),
                                   OrderCallReason.CHANGE_PRICE);

    public final static OrderEventTypeData changeAmountData =
            new OrderEventTypeData(EnumSet.of(CHANGED_AMOUNT),
                                   EnumSet.of(CHANGE_AMOUNT_REJECTED),
                                   OrderCallReason.CHANGE_AMOUNT);

    public final static OrderEventTypeData changeSLData =
            new OrderEventTypeData(EnumSet.of(CHANGED_SL),
                                   EnumSet.of(CHANGE_SL_REJECTED),
                                   OrderCallReason.CHANGE_SL);

    public final static OrderEventTypeData changeTPData =
            new OrderEventTypeData(EnumSet.of(CHANGED_TP),
                                   EnumSet.of(CHANGE_TP_REJECTED),
                                   OrderCallReason.CHANGE_TP);
}
