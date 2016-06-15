package com.jforex.programming.order.event;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.JFHotObservable;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;

import rx.Observable;

public class OrderEventGateway {

    private final JFHotObservable<OrderEvent> orderEventPublisher = new JFHotObservable<>();
    private final Queue<OrderCallRequest> changeRequestQueue = new ConcurrentLinkedQueue<>();

    public final static Set<IMessage.Type> changeEventTypes =
            Sets.immutableEnumSet(IMessage.Type.ORDER_CHANGED_OK,
                                  IMessage.Type.ORDER_CHANGED_REJECTED);

    public final static Set<OrderCallReason> changeReasons =
            Sets.immutableEnumSet(OrderCallReason.CHANGE_GOOD_TILL_TIME,
                                  OrderCallReason.CHANGE_LABEL,
                                  OrderCallReason.CHANGE_OPEN_PRICE,
                                  OrderCallReason.CHANGE_REQUESTED_AMOUNT,
                                  OrderCallReason.CHANGE_STOP_LOSS_PRICE,
                                  OrderCallReason.CHANGE_TAKE_PROFIT_PRICE);

    private static final Logger logger = LogManager.getLogger(OrderEventGateway.class);

    public Observable<OrderEvent> observable() {
        return orderEventPublisher.observable();
    }

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        if (changeReasons.contains(orderCallRequest.reason()))
            changeRequestQueue.add(orderCallRequest);
    }

    public void onOrderMessageData(final OrderMessageData orderMessageData) {
        final IOrder order = orderMessageData.order();
        final OrderEventType orderEventType = orderEventTypeFromData(orderMessageData);
        logger.debug("Received order event for " + order.getLabel()
                + " type " + orderEventType + " state " + order.getState());
        orderEventPublisher.onNext(new OrderEvent(order, orderEventType));
    }

    private final OrderEventType orderEventTypeFromData(final OrderMessageData orderMessageData) {
        if (changeRequestQueue.isEmpty())
            return OrderEventTypeEvaluator.get(orderMessageData);
        if (changeRequestQueue.peek().order() != orderMessageData.order())
            return OrderEventTypeEvaluator.get(orderMessageData);
        if (changeEventTypes.contains(orderMessageData.messageType()))
            return OrderEventTypeEvaluator.getWithCallReason(orderMessageData, changeRequestQueue.poll().reason());
        return OrderEventTypeEvaluator.get(orderMessageData);
    }
}
