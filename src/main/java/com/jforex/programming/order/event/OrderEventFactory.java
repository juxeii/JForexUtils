package com.jforex.programming.order.event;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;

public class OrderEventFactory {

    private final Queue<OrderCallRequest> changeRequestQueue = new ConcurrentLinkedQueue<>();

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

    public OrderEvent fromMessage(final IMessage message) {
        OrderEventType orderEventType = evaluateType(message);
        if (isForChangeReason(message, orderEventType))
            orderEventType = OrderEventMapperData.mapByChangeReject(changeRequestQueue.poll().reason());

        return new OrderEvent(message.getOrder(), orderEventType);
    }

    private final boolean isForChangeReason(final IMessage message,
                                            final OrderEventType orderEventType) {
        return !changeRequestQueue.isEmpty()
                && changeRequestQueue.peek().order() == message.getOrder()
                && orderEventType == OrderEventType.CHANGED_REJECTED;
    }

    private final OrderEventType evaluateType(final IMessage message) {
        final Set<Reason> reasons = message.getReasons();
        return isEventByReason(reasons)
                ? OrderEventMapperData.mapByReason(reasons)
                : OrderEventMapperData.mapByType(message.getOrder(), message.getType());
    }

    private final boolean isEventByReason(final Set<Reason> reasons) {
        return reasons.size() == 1;
    }
}
