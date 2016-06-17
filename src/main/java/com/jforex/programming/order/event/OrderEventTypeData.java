package com.jforex.programming.order.event;

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
import static com.jforex.programming.order.event.OrderEventType.OPENPRICE_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.REQUESTED_AMOUNT_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.SL_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.TP_CHANGE_OK;

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
            new OrderEventTypeData(EnumSet.of(FULL_FILL_OK, SUBMIT_CONDITIONAL_OK, PARTIAL_FILL_OK),
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
            new OrderEventTypeData(EnumSet.of(LABEL_CHANGE_OK),
                                   EnumSet.of(CHANGE_LABEL_REJECTED),
                                   OrderCallReason.CHANGE_LABEL);

    public final static OrderEventTypeData changeGTTData =
            new OrderEventTypeData(EnumSet.of(GTT_CHANGE_OK),
                                   EnumSet.of(CHANGE_GTT_REJECTED),
                                   OrderCallReason.CHANGE_GTT);

    public final static OrderEventTypeData changeOpenPriceData =
            new OrderEventTypeData(EnumSet.of(OPENPRICE_CHANGE_OK),
                                   EnumSet.of(CHANGE_OPENPRICE_REJECTED),
                                   OrderCallReason.CHANGE_OPEN_PRICE);

    public final static OrderEventTypeData changeAmountData =
            new OrderEventTypeData(EnumSet.of(REQUESTED_AMOUNT_CHANGE_OK),
                                   EnumSet.of(CHANGE_AMOUNT_REJECTED),
                                   OrderCallReason.CHANGE_REQUESTED_AMOUNT);

    public final static OrderEventTypeData changeSLData =
            new OrderEventTypeData(EnumSet.of(SL_CHANGE_OK),
                                   EnumSet.of(CHANGE_SL_REJECTED),
                                   OrderCallReason.CHANGE_SL);

    public final static OrderEventTypeData changeTPData =
            new OrderEventTypeData(EnumSet.of(TP_CHANGE_OK),
                                   EnumSet.of(CHANGE_TP_REJECTED),
                                   OrderCallReason.CHANGE_TP);
}
