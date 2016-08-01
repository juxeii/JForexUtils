package com.jforex.programming.order.event;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;

public class OrderEventMapper {

    private final Queue<OrderCallRequest> changeRequestQueue = new ConcurrentLinkedQueue<>();

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
            Maps.immutableEnumMap(ImmutableMap
                    .<OrderCallReason, OrderEventType> builder()
                    .put(OrderCallReason.CHANGE_AMOUNT, OrderEventType.CHANGE_AMOUNT_REJECTED)
                    .put(OrderCallReason.CHANGE_LABEL, OrderEventType.CHANGE_LABEL_REJECTED)
                    .put(OrderCallReason.CHANGE_GTT, OrderEventType.CHANGE_GTT_REJECTED)
                    .put(OrderCallReason.CHANGE_PRICE, OrderEventType.CHANGE_PRICE_REJECTED)
                    .put(OrderCallReason.CHANGE_SL, OrderEventType.CHANGE_SL_REJECTED)
                    .put(OrderCallReason.CHANGE_TP, OrderEventType.CHANGE_TP_REJECTED)
                    .build());

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        if (changeReasons.contains(orderCallRequest.reason()))
            changeRequestQueue.add(orderCallRequest);
    }

    public OrderEventType get(final OrderMessageData orderMessageData) {
        return isNotForChangeReason(orderMessageData)
                ? evaluate(orderMessageData)
                : getForChangeReason(orderMessageData, changeRequestQueue.poll().reason());
    }

    private final boolean isNotForChangeReason(final OrderMessageData orderMessageData) {
        return changeRequestQueue.isEmpty()
                || changeRequestQueue.peek().order() != orderMessageData.order()
                || !changeEventTypes.contains(orderMessageData.messageType());
    }

    private final OrderEventType getForChangeReason(final OrderMessageData orderEventData,
                                                    final OrderCallReason orderCallReason) {
        final OrderEventType orderEventType = evaluate(orderEventData);
        return orderEventType == OrderEventType.CHANGED_REJECTED
                ? mapChangeReject(orderCallReason)
                : orderEventType;
    }

    private final OrderEventType evaluate(final OrderMessageData orderEventData) {
        final Set<Reason> reasons = orderEventData.messageReasons();
        return isEventByReason(reasons)
                ? eventByReason(reasons)
                : eventByType(orderEventData);
    }

    private final boolean isEventByReason(final Set<Reason> reasons) {
        return reasons.size() == 1;
    }

    private final OrderEventType eventByType(final OrderMessageData orderEventData) {
        return TypeEventMapper.map(orderEventData.order(), orderEventData.messageType());
    }

    private final OrderEventType eventByReason(final Set<Reason> reasons) {
        return ReasonEventMapper.map(reasons.iterator().next());
    }

    private final OrderEventType mapChangeReject(final OrderCallReason orderCallReason) {
        return changeRejectEventByRequest.get(orderCallReason);
    }
}
