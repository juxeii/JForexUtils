package com.jforex.programming.order.event;

import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;

public class OrderEventFactory {

    private final Queue<OrderCallRequest> callRequestQueue = new ConcurrentLinkedQueue<>();
    private final Set<IOrder> registeredOrders = Collections.newSetFromMap(new WeakHashMap<IOrder, Boolean>());

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        callRequestQueue.add(orderCallRequest);
    }

    public OrderEvent fromMessage(final IMessage message) {
        final IOrder order = message.getOrder();
        final OrderEventType orderEventType = calculateType(message);
        final OrderEvent orderEvent = evaluateToOrderEvent(order, orderEventType);
        cleanUpRegisteredOrder(order);

        return orderEvent;
    }

    private final OrderEvent evaluateToOrderEvent(final IOrder order,
                                                  final OrderEventType orderEventType) {
        return isOrderNextInRequestQueue(order)
                ? eventForQueuedOrder(order, orderEventType)
                : eventForNotQueuedOrder(order, orderEventType);
    }

    private final void cleanUpRegisteredOrder(final IOrder order) {
        if (OrderStaticUtil.isClosed.test(order) ||
                OrderStaticUtil.isCanceled.test(order))
            registeredOrders.remove(order);
    }

    private final OrderEvent eventForQueuedOrder(final IOrder order,
                                                 final OrderEventType rawOrderEventType) {
        registeredOrders.add(order);
        return new OrderEvent(order,
                              refineTypeForRegisteredOrder(rawOrderEventType),
                              true);
    }

    private final OrderEvent eventForNotQueuedOrder(final IOrder order,
                                                    final OrderEventType orderEventType) {
        final boolean isInternal = registeredOrders.contains(order);
        return new OrderEvent(order,
                              orderEventType,
                              isInternal);
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
