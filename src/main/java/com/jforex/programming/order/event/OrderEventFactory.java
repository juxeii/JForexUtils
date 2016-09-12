package com.jforex.programming.order.event;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;

public class OrderEventFactory {

    private final Queue<OrderCallRequest> callRequestQueue = new ConcurrentLinkedQueue<>();

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        callRequestQueue.add(orderCallRequest);
    }

    public OrderEvent fromMessage(final IMessage message) {
        final IOrder order = message.getOrder();
        final OrderEventType orderEventType = calculateType(message);

        return isOrderNextInRequestQueue(order)
                ? eventForRegisteredOrder(order, orderEventType)
                : new OrderEvent(order,
                                 orderEventType,
                                 false);
    }

    private final OrderEvent eventForRegisteredOrder(final IOrder order,
                                                     final OrderEventType rawOrderEventType) {
        return new OrderEvent(order,
                              refineTypeForRegisteredOrder(rawOrderEventType),
                              true);
    }

    private final OrderEventType refineTypeForRegisteredOrder(final OrderEventType orderEventType) {
        final OrderCallReason callReason = callRequestQueue.poll().reason();
        return orderEventType == OrderEventType.CHANGED_REJECTED
                ? OrderEventTypeMapper.byChangeCallReason(callReason)
                : orderEventType;
    }

    private final OrderEventType calculateType(final IMessage message) {
        final Set<Reason> reasons = message.getReasons();
        return reasons.size() == 1
                ? OrderEventTypeMapper.byMessageReason(reasons.iterator().next())
                : calculateTypeByMessageType(message);
    }

    private final OrderEventType calculateTypeByMessageType(final IMessage message) {
        final IOrder order = message.getOrder();
        return OrderEventTypeMapper.byMessageType(message.getType(), order);
    }

    private final boolean isOrderNextInRequestQueue(final IOrder messageOrder) {
        return !callRequestQueue.isEmpty()
                && callRequestQueue.peek().order().equals(messageOrder);
    }
}
