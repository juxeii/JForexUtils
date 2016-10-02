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

import io.reactivex.Observable;

public class OrderEventFactory {

    private final ConcurrentMap<IOrder, Queue<OrderCallReason>> callReasonByOrder =
            new MapMaker().weakKeys().makeMap();

    public OrderEventFactory(final Observable<OrderCallRequest> callRequestObservable) {
        callRequestObservable.subscribe(this::registerOrderCallRequest);
    }

    private void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
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
                OrderStaticUtil.isCanceled.test(order)) {
            if (callReasonByOrder.containsKey(order))
                callReasonByOrder.remove(order).clear();
        }
    }

    private final OrderEvent eventForInternalOrder(final IOrder order,
                                                   final OrderEventType rawOrderEventType) {
        final OrderEventType orderEventType = infoEvents.contains(rawOrderEventType)
                ? rawOrderEventType
                : eventTypeForDoneTrigger(order, rawOrderEventType);

        return new OrderEvent(order,
                              orderEventType,
                              true);
    }

    private final OrderEvent eventForExternalOrder(final IOrder order,
                                                   final OrderEventType orderEventType) {
        return new OrderEvent(order,
                              orderEventType,
                              false);
    }

    private final OrderEventType eventTypeForDoneTrigger(final IOrder order,
                                                         final OrderEventType orderEventType) {
        return callReasonByOrder.get(order).isEmpty()
                ? orderEventType
                : refineEventType(order, orderEventType);
    }

    private final OrderEventType refineEventType(final IOrder order,
                                                 final OrderEventType orderEventType) {
        final OrderCallReason callReason = callReasonByOrder.get(order).poll();
        return orderEventType == OrderEventType.CHANGED_REJECTED
                ? OrderEventTypeMapper.byChangeCallReason(callReason)
                : orderEventType;
    }
}
