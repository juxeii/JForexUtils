package com.jforex.programming.order.event;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.dukascopy.api.IOrder;
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
        return new OrderEvent(message.getOrder(), calculateType(message));
    }

    private final OrderEventType calculateType(final IMessage message) {
        final Set<Reason> reasons = message.getReasons();
        return reasons.size() == 1
                ? OrderEventMapperData.mapByReason(reasons)
                : calculateTypeByMessageType(message);
    }

    private final OrderEventType calculateTypeByMessageType(final IMessage message) {
        final IOrder order = message.getOrder();
        final OrderEventType orderEventType = OrderEventMapperData.mapByType(order, message.getType());
        return isTypeForChangeReason(order, orderEventType)
                ? OrderEventMapperData.mapByChangeReject(changeRequestQueue.poll().reason())
                : orderEventType;
    }

    private final boolean isTypeForChangeReason(final IOrder order,
                                                final OrderEventType orderEventType) {
        return !changeRequestQueue.isEmpty()
                && changeRequestQueue.peek().order() == order
                && orderEventType == OrderEventType.CHANGED_REJECTED;
    }
}
