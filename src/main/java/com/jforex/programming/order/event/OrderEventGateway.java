package com.jforex.programming.order.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.JFHotObservable;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallRequest;

import com.dukascopy.api.IOrder;

import rx.Observable;

public class OrderEventGateway {

    private final JFHotObservable<OrderEvent> orderEventPublisher = new JFHotObservable<>();
    private final Queue<OrderCallRequest> callRequestQueue = new ConcurrentLinkedQueue<>();

    private static final Logger logger = LogManager.getLogger(OrderEventGateway.class);

    public Observable<OrderEvent> observable() {
        return orderEventPublisher.observable();
    }

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        callRequestQueue.add(orderCallRequest);
    }

    public void onOrderMessageData(final OrderMessageData orderMessageData) {
        final IOrder order = orderMessageData.order();
        final OrderEventType orderEventType = orderEventTypeFromData(orderMessageData);
        logger.debug("Received order event for " + order.getLabel()
                + " type " + orderEventType + " state " + order.getState());
        orderEventPublisher.onNext(new OrderEvent(order, orderEventType));
    }

    private final OrderEventType orderEventTypeFromData(final OrderMessageData orderMessageData) {
        if (callRequestQueue.isEmpty())
            return OrderEventTypeEvaluator.get(orderMessageData);

        final OrderCallRequest orderCallRequest = callRequestQueue.poll();
        return orderCallRequest.order() == orderMessageData.order()
                ? OrderEventTypeEvaluator.get(orderMessageData, orderCallRequest.reason())
                : OrderEventTypeEvaluator.get(orderMessageData);
    }
}
