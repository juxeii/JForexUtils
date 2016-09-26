package com.jforex.programming.order.event;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.dukascopy.api.IOrder;
import com.google.common.collect.MapMaker;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;

public class OrderEventFactory {

    private final Queue<OrderCallRequest> callRequestQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentMap<IOrder, Boolean> ordersOfQueue =
            new MapMaker().weakKeys().makeMap();

    private static final Logger logger = LogManager.getLogger(OrderEventFactory.class);

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        logger.info("Queueing callrequest " + orderCallRequest.order().getLabel() + " reason "
                + orderCallRequest.reason());
        callRequestQueue.add(orderCallRequest);
        ordersOfQueue.put(orderCallRequest.order(), true);
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
            ordersOfQueue.remove(order);

    }

    private final OrderEvent eventForQueuedOrder(final IOrder order,
                                                 final OrderEventType rawOrderEventType) {
        return new OrderEvent(order,
                              refineTypeForRegisteredOrder(rawOrderEventType),
                              true);
    }

    private final OrderEvent eventForNotQueuedOrder(final IOrder order,
                                                    final OrderEventType orderEventType) {
        final boolean isInternal = ordersOfQueue.keySet().contains(order);
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
