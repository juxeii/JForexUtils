package com.jforex.programming.order.event;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.MapMaker;
import com.jforex.programming.misc.JFObservable;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallRequest;

import com.dukascopy.api.IOrder;

import rx.Observable;

public class OrderEventGateway {

    private final JFObservable<OrderEvent> orderEventPublisher = new JFObservable<>();
    private final ConcurrentMap<IOrder, Queue<OrderCallRequest>> callRequestByOrder =
            new MapMaker().weakKeys().makeMap();

    private static final Logger logger = LogManager.getLogger(OrderEventGateway.class);

    public Observable<OrderEvent> observable() {
        return orderEventPublisher.get();
    }

    public void registerOrderRequest(final IOrder order,
                                     final OrderCallRequest orderCallRequest) {
        callRequestByOrder.putIfAbsent(order, new ConcurrentLinkedQueue<>());
        callRequestByOrder.get(order).add(orderCallRequest);
    }

    public void onOrderMessageData(final OrderMessageData orderMessageData) {
        final IOrder order = orderMessageData.order();
        final OrderEventType orderEventType = orderEventTypeFromData(orderMessageData);
        logger.debug("Received order event for " + order.getLabel()
                + " type " + orderEventType + " state " + order.getState());
        orderEventPublisher.onNext(new OrderEvent(order, orderEventType));
    }

    private final OrderEventType orderEventTypeFromData(final OrderMessageData orderMessageData) {
        return callRequestByOrder.containsKey(orderMessageData.order())
                ? orderEventWithQueuePresent(orderMessageData)
                : OrderEventTypeEvaluator.get(orderMessageData, Optional.empty());
    }

    private final OrderEventType orderEventWithQueuePresent(final OrderMessageData orderMessageData) {
        final IOrder order = orderMessageData.order();
        Optional<OrderCallRequest> callRequestOpt = Optional.empty();
        if (!callRequestByOrder.get(order).isEmpty())
            callRequestOpt = Optional.of(callRequestByOrder.get(order).poll());
        else
            callRequestByOrder.remove(order);

        return OrderEventTypeEvaluator.get(orderMessageData, callRequestOpt);
    }
}
