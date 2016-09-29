package com.jforex.programming.order.event;

import static com.jforex.programming.order.event.OrderEventTypeSets.infoEvents;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.dukascopy.api.IOrder;
import com.google.common.collect.MapMaker;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;

public class OrderEventFactory {

    private final ConcurrentMap<IOrder, Queue<OrderCallReason>> callReasonByOrder =
            new MapMaker().weakKeys().makeMap();

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        final IOrder order = orderCallRequest.order();
        callReasonByOrder.putIfAbsent(order, new ConcurrentLinkedQueue<>());
        callReasonByOrder.get(order).add(orderCallRequest.reason());
    }

    public OrderEvent fromMessage(final IMessage message) {
        final IOrder order = message.getOrder();
        final OrderEventType orderEventType = calculateType(message);
        final OrderEvent orderEvent = evaluateToOrderEvent(order, orderEventType);
        cleanUpRegisteredOrder(order);

        return orderEvent;
    }

    private final OrderEventType calculateType(final IMessage message) {
        final Set<Reason> reasons = message.getReasons();
        return reasons.size() == 1
                ? OrderEventTypeMapper.byMessageReason(reasons.iterator().next())
                : OrderEventTypeMapper.byMessageType(message.getType(), message.getOrder());
    }

    private final OrderEvent evaluateToOrderEvent(final IOrder order,
                                                  final OrderEventType orderEventType) {
        return callReasonByOrder.keySet().contains(order)
                ? eventForInternalOrder(order, orderEventType)
                : eventForExternalOrder(order, orderEventType);
    }

    private final void cleanUpRegisteredOrder(final IOrder order) {
        if (OrderStaticUtil.isClosed.test(order) ||
                OrderStaticUtil.isCanceled.test(order))
            callReasonByOrder.remove(order).clear();
    }

    private final OrderEvent eventForInternalOrder(final IOrder order,
                                                   final OrderEventType orderEventType) {
        return infoEvents.contains(orderEventType)
                ? new OrderEvent(order,
                                 orderEventType,
                                 true)
                : eventForDoneTrigger(order, orderEventType);
    }

    private final OrderEvent eventForExternalOrder(final IOrder order,
                                                   final OrderEventType orderEventType) {
        return new OrderEvent(order,
                              orderEventType,
                              false);
    }

    private final OrderEvent eventForDoneTrigger(final IOrder order,
                                                 final OrderEventType orderEventType) {
        if (callReasonByOrder.get(order).isEmpty())
            return new OrderEvent(order,
                                  orderEventType,
                                  true);

        final OrderCallReason callReason = callReasonByOrder.get(order).poll();
        final OrderEventType refinedEventType = orderEventType == OrderEventType.CHANGED_REJECTED
                ? OrderEventTypeMapper.byChangeCallReason(callReason)
                : orderEventType;

        return new OrderEvent(order,
                              refinedEventType,
                              true);
    }
}
