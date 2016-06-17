package com.jforex.programming.order.event;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;

import com.dukascopy.api.IMessage.Reason;

public class OrderEventMapper {

    private final Queue<OrderCallRequest> changeRequestQueue = new ConcurrentLinkedQueue<>();

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        if (ChangeEventMapper.changeReasons.contains(orderCallRequest.reason()))
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
                || !ChangeEventMapper.changeEventTypes.contains(orderMessageData.messageType());
    }

    private final OrderEventType getForChangeReason(final OrderMessageData orderEventData,
                                                    final OrderCallReason orderCallReason) {
        final OrderEventType orderEventType = evaluate(orderEventData);
        return refineWithCallReason(orderEventType, orderCallReason);
    }

    private final OrderEventType refineWithCallReason(final OrderEventType orderEventType,
                                                      final OrderCallReason orderCallReason) {
        return orderEventType == OrderEventType.CHANGE_REJECTED
                ? ChangeEventMapper.map(orderCallReason)
                : orderEventType;
    }

    private final OrderEventType evaluate(final OrderMessageData orderEventData) {
        return isEventByReason(orderEventData.messageReasons())
                ? eventByReason(orderEventData.messageReasons())
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
}
