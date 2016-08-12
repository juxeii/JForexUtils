package com.jforex.programming.order.event;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;

public class OrderEventMapper {

    private final Queue<OrderCallRequest> changeRequestQueue = new ConcurrentLinkedQueue<>();

    private static final Set<IMessage.Type> changeEventTypes =
            Sets.immutableEnumSet(IMessage.Type.ORDER_CHANGED_OK,
                                  IMessage.Type.ORDER_CHANGED_REJECTED);

    private static final Set<OrderCallReason> changeReasons =
            Sets.immutableEnumSet(OrderCallReason.CHANGE_GTT,
                                  OrderCallReason.CHANGE_LABEL,
                                  OrderCallReason.CHANGE_PRICE,
                                  OrderCallReason.CHANGE_AMOUNT,
                                  OrderCallReason.CHANGE_SL,
                                  OrderCallReason.CHANGE_TP);

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        if (changeReasons.contains(orderCallRequest.reason()))
            changeRequestQueue.add(orderCallRequest);
    }

    public OrderEventType get(final IMessage message) {
        return isNotForChangeReason(message)
                ? evaluate(message)
                : getForChangeReason(message, changeRequestQueue.poll().reason());
    }

    private final boolean isNotForChangeReason(final IMessage message) {
        return changeRequestQueue.isEmpty()
                || changeRequestQueue.peek().order() != message.getOrder()
                || !changeEventTypes.contains(message.getType());
    }

    private final OrderEventType getForChangeReason(final IMessage message,
                                                    final OrderCallReason orderCallReason) {
        final OrderEventType orderEventType = evaluate(message);
        return orderEventType == OrderEventType.CHANGED_REJECTED
                ? OrderEventMapperData.mapByChangeReject(orderCallReason)
                : orderEventType;
    }

    private final OrderEventType evaluate(final IMessage message) {
        final Set<Reason> reasons = message.getReasons();
        return isEventByReason(reasons)
                ? OrderEventMapperData.mapByReason(reasons)
                : OrderEventMapperData.mapByType(message.getOrder(), message.getType());
    }

    private final boolean isEventByReason(final Set<Reason> reasons) {
        return reasons.size() == 1;
    }
}
