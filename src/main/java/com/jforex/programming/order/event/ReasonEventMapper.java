package com.jforex.programming.order.event;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import com.dukascopy.api.IMessage;

public final class ReasonEventMapper {

    private final static Map<IMessage.Reason, OrderEventType> orderEventByReason =
            Maps.immutableEnumMap(ImmutableMap.<IMessage.Reason, OrderEventType> builder()
                    .put(IMessage.Reason.ORDER_FULLY_FILLED, OrderEventType.FULL_FILL_OK)
                    .put(IMessage.Reason.ORDER_CLOSED_BY_MERGE, OrderEventType.CLOSED_BY_MERGE)
                    .put(IMessage.Reason.ORDER_CLOSED_BY_SL, OrderEventType.CLOSED_BY_SL)
                    .put(IMessage.Reason.ORDER_CLOSED_BY_TP, OrderEventType.CLOSED_BY_TP)
                    .put(IMessage.Reason.ORDER_CHANGED_SL, OrderEventType.SL_CHANGE_OK)
                    .put(IMessage.Reason.ORDER_CHANGED_TP, OrderEventType.TP_CHANGE_OK)
                    .put(IMessage.Reason.ORDER_CHANGED_AMOUNT, OrderEventType.REQUESTED_AMOUNT_CHANGE_OK)
                    .put(IMessage.Reason.ORDER_CHANGED_PRICE, OrderEventType.OPENPRICE_CHANGE_OK)
                    .put(IMessage.Reason.ORDER_CHANGED_GTT, OrderEventType.GTT_CHANGE_OK)
                    .put(IMessage.Reason.ORDER_CHANGED_LABEL, OrderEventType.LABEL_CHANGE_OK)
                    .build());

    public final static OrderEventType map(final IMessage.Reason reason) {
        return orderEventByReason.get(reason);
    }
}
