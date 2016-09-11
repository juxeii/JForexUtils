package com.jforex.programming.order.event;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallRequest;

public class MessageToOrderEvent {

    private final Queue<OrderCallRequest> callRequestQueue = new ConcurrentLinkedQueue<>();

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        callRequestQueue.add(orderCallRequest);
    }

    public OrderEvent fromMessage(final IMessage message) {
        return new OrderEvent(message.getOrder(),
                              calculateType(message),
                              isOrderInRequestQueue(message.getOrder()));
    }

    private final boolean isOrderInRequestQueue(final IOrder messageOrder) {
        return !callRequestQueue.isEmpty()
                && callRequestQueue.peek().order().equals(messageOrder);
    }

    private final OrderEventType calculateType(final IMessage message) {
        final Set<Reason> reasons = message.getReasons();
        return reasons.size() == 1
                ? OrderEventTypeMapper.byMessageReason(reasons.iterator().next())
                : calculateTypeByMessageType(message);
    }

    private final OrderEventType calculateTypeByMessageType(final IMessage message) {
        final IOrder order = message.getOrder();
        final OrderEventType orderEventType = OrderEventTypeMapper.byMessageType(message.getType(), order);
        return isTypeForChangeReason(order, orderEventType)
                ? OrderEventTypeMapper.byCallReason(callRequestQueue.poll().reason())
                : orderEventType;
    }

    private final boolean isTypeForChangeReason(final IOrder order,
                                                final OrderEventType orderEventType) {
        return isOrderInRequestQueue(order) && orderEventType == OrderEventType.CHANGED_REJECTED;
    }
}
