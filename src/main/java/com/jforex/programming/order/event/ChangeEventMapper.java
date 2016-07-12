package com.jforex.programming.order.event;

import java.util.Map;
import java.util.Set;

import com.dukascopy.api.IMessage;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;

public final class ChangeEventMapper {

    private ChangeEventMapper() {
    }

    public static final Set<IMessage.Type> changeEventTypes =
            Sets.immutableEnumSet(IMessage.Type.ORDER_CHANGED_OK,
                                  IMessage.Type.ORDER_CHANGED_REJECTED);

    public static final Set<OrderCallReason> changeReasons =
            Sets.immutableEnumSet(OrderCallReason.CHANGE_GTT,
                                  OrderCallReason.CHANGE_LABEL,
                                  OrderCallReason.CHANGE_PRICE,
                                  OrderCallReason.CHANGE_AMOUNT,
                                  OrderCallReason.CHANGE_SL,
                                  OrderCallReason.CHANGE_TP);

    private static final Map<OrderCallReason, OrderEventType> changeRejectEventByRequest =
            Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, OrderEventType> builder()
                    .put(OrderCallReason.CHANGE_AMOUNT, OrderEventType.CHANGE_AMOUNT_REJECTED)
                    .put(OrderCallReason.CHANGE_LABEL, OrderEventType.CHANGE_LABEL_REJECTED)
                    .put(OrderCallReason.CHANGE_GTT, OrderEventType.CHANGE_GTT_REJECTED)
                    .put(OrderCallReason.CHANGE_PRICE, OrderEventType.CHANGE_PRICE_REJECTED)
                    .put(OrderCallReason.CHANGE_SL, OrderEventType.CHANGE_SL_REJECTED)
                    .put(OrderCallReason.CHANGE_TP, OrderEventType.CHANGE_TP_REJECTED)
                    .build());

    public static final OrderEventType map(final OrderCallReason orderCallReason) {
        return changeRejectEventByRequest.get(orderCallReason);
    }
}
