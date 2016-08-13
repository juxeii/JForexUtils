package com.jforex.programming.order.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JFHotSubject;
import com.jforex.programming.order.call.OrderCallRequest;

import rx.Observable;

public class OrderEventGateway {

    private final JFHotSubject<OrderEvent> orderEventPublisher = new JFHotSubject<>();
    private final OrderEventFactory orderEventMapper;

    private static final Logger logger = LogManager.getLogger(OrderEventGateway.class);

    public OrderEventGateway(final Observable<IMessage> messageObservable,
                             final OrderEventFactory orderEventMapper) {
        this.orderEventMapper = orderEventMapper;

        messageObservable
            .filter(message -> message.getOrder() != null)
            .subscribe(this::onOrderMessage);
    }

    public Observable<OrderEvent> observable() {
        return orderEventPublisher.observable();
    }

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        orderEventMapper.registerOrderCallRequest(orderCallRequest);
    }

    private void onOrderMessage(final IMessage message) {
        final IOrder order = message.getOrder();
        final OrderEvent orderEvent = orderEventMapper.fromMessage(message);
        logger.debug("Received order event for " + order.getLabel()
                + " type " + orderEvent.type() + " state " + order.getState());
        orderEventPublisher.onNext(orderEvent);
    }
}
