package com.jforex.programming.order.event;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import com.dukascopy.api.IMessage;

public final class ReasonEventMapper {

    private final static Map<IMessage.Reason, OrderEventType> orderEventByReason =
            Maps.immutableEnumMap(ImmutableMap.<IMessage.Reason, OrderEventType> builder()
                    .put(IMessage.Reason.ORDER_FULLY_FILLED, OrderEventType.FULLY_FILLED)
                    .put(IMessage.Reason.ORDER_CLOSED_BY_MERGE, OrderEventType.CLOSED_BY_MERGE)
                    .put(IMessage.Reason.ORDER_CLOSED_BY_SL, OrderEventType.CLOSED_BY_SL)
                    .put(IMessage.Reason.ORDER_CLOSED_BY_TP, OrderEventType.CLOSED_BY_TP)
                    .put(IMessage.Reason.ORDER_CHANGED_SL, OrderEventType.CHANGED_SL)
                    .put(IMessage.Reason.ORDER_CHANGED_TP, OrderEventType.CHANGED_TP)
                    .put(IMessage.Reason.ORDER_CHANGED_AMOUNT, OrderEventType.CHANGED_AMOUNT)
                    .put(IMessage.Reason.ORDER_CHANGED_PRICE, OrderEventType.CHANGED_PRICE)
                    .put(IMessage.Reason.ORDER_CHANGED_GTT, OrderEventType.CHANGED_GTT)
                    .put(IMessage.Reason.ORDER_CHANGED_LABEL, OrderEventType.CHANGED_LABEL)
                    .build());

    public final static OrderEventType map(final IMessage.Reason reason) {
        return orderEventByReason.get(reason);
    }
}
