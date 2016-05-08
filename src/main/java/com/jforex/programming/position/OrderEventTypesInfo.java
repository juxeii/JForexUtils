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
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.PRICE_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.SL_CHANGE_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.TP_CHANGE_OK;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEventType;

public final class OrderEventTypesInfo {

    private final ImmutableSet<OrderEventType> doneEventTypes;
    private final ImmutableSet<OrderEventType> intermediateTypes;
    private final ImmutableSet<OrderEventType> rejectEventTypes;
    private final ImmutableSet<OrderEventType> allTypes;

    private OrderEventTypesInfo(final EnumSet<OrderEventType> doneEventTypes,
                                final EnumSet<OrderEventType> rejectEventTypes) {
        this.doneEventTypes = Sets.immutableEnumSet(doneEventTypes);
        this.rejectEventTypes = Sets.immutableEnumSet(rejectEventTypes);
        intermediateTypes = Sets.immutableEnumSet(SUBMIT_OK);

        final EnumSet<OrderEventType> tmpAllTypes = doneEventTypes;
        tmpAllTypes.addAll(intermediateTypes);
        tmpAllTypes.addAll(rejectEventTypes);
        allTypes = Sets.immutableEnumSet(tmpAllTypes);
    }

    public final boolean isDoneType(final OrderEventType orderEventType) {
        return doneEventTypes.contains(orderEventType);
    }

    public final boolean isIntermediateType(final OrderEventType orderEventType) {
        return intermediateTypes.contains(orderEventType);
    }

    public final boolean isRejectType(final OrderEventType orderEventType) {
        return rejectEventTypes.contains(orderEventType);
    }

    public final Set<OrderEventType> all() {
        return allTypes;
    }

    public final static OrderEventTypesInfo submitEvents =
            new OrderEventTypesInfo(EnumSet.of(FULL_FILL_OK, PARTIAL_FILL_OK),
                                    EnumSet.of(FILL_REJECTED, SUBMIT_REJECTED));

    public final static OrderEventTypesInfo mergeEvents =
            new OrderEventTypesInfo(EnumSet.of(MERGE_OK, MERGE_CLOSE_OK),
                                    EnumSet.of(MERGE_REJECTED));

    public final static OrderEventTypesInfo closeEvents =
            new OrderEventTypesInfo(EnumSet.of(CLOSE_OK),
                                    EnumSet.of(CLOSE_REJECTED));

    public final static OrderEventTypesInfo changeLabelEvents =
            new OrderEventTypesInfo(EnumSet.of(LABEL_CHANGE_OK),
                                    EnumSet.of(CHANGE_LABEL_REJECTED));

    public final static OrderEventTypesInfo changeGTTEvents =
            new OrderEventTypesInfo(EnumSet.of(GTT_CHANGE_OK),
                                    EnumSet.of(CHANGE_GTT_REJECTED));

    public final static OrderEventTypesInfo changeOpenPriceEvents =
            new OrderEventTypesInfo(EnumSet.of(PRICE_CHANGE_OK),
                                    EnumSet.of(CHANGE_OPENPRICE_REJECTED));

    public final static OrderEventTypesInfo changeAmountEvents =
            new OrderEventTypesInfo(EnumSet.of(AMOUNT_CHANGE_OK),
                                    EnumSet.of(CHANGE_AMOUNT_REJECTED));

    public final static OrderEventTypesInfo changeSLEvents =
            new OrderEventTypesInfo(EnumSet.of(SL_CHANGE_OK),
                                    EnumSet.of(CHANGE_SL_REJECTED));

    public final static OrderEventTypesInfo changeTPEvents =
            new OrderEventTypesInfo(EnumSet.of(TP_CHANGE_OK),
                                    EnumSet.of(CHANGE_TP_REJECTED));
}
