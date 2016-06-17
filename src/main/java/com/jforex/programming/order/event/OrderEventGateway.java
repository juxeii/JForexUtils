package com.jforex.programming.order.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.JFHotObservable;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallRequest;

import com.dukascopy.api.IOrder;

import rx.Observable;

public class OrderEventGateway {

    private final JFHotObservable<OrderEvent> orderEventPublisher = new JFHotObservable<>();
    private final OrderEventMapper orderEventMapper;

    private static final Logger logger = LogManager.getLogger(OrderEventGateway.class);

    public OrderEventGateway(final OrderEventMapper orderEventMapper) {
        this.orderEventMapper = orderEventMapper;
    }

    public Observable<OrderEvent> observable() {
        return orderEventPublisher.observable();
    }

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        orderEventMapper.registerOrderCallRequest(orderCallRequest);
    }

    public void onOrderMessageData(final OrderMessageData orderMessageData) {
        final IOrder order = orderMessageData.order();
        final OrderEventType orderEventType = orderEventMapper.get(orderMessageData);
        logger.debug("Received order event for " + order.getLabel()
                + " type " + orderEventType + " state " + order.getState());
        orderEventPublisher.onNext(new OrderEvent(order, orderEventType));
    }
}
