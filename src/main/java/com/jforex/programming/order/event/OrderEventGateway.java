package com.jforex.programming.order.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JFHotObservable;

import io.reactivex.Observable;

public class OrderEventGateway {

    private final JFHotObservable<OrderEvent> orderEventPublisher = new JFHotObservable<>();
    private final OrderEventFactory orderEventFactory;

    private static final Logger logger = LogManager.getLogger(OrderEventGateway.class);

    public OrderEventGateway(final Observable<IMessage> messageObservable,
                             final OrderEventFactory orderEventFactory) {
        this.orderEventFactory = orderEventFactory;

        messageObservable
            .filter(message -> message.getOrder() != null)
            .subscribe(this::onOrderMessage);
    }

    public Observable<OrderEvent> observable() {
        return orderEventPublisher.observable();
    }

    private void onOrderMessage(final IMessage message) {
        final OrderEvent orderEvent = orderEventFactory.fromMessage(message);
        final IOrder order = orderEvent.order();
        logger.debug("Received order event with label " + order.getLabel()
                + " for " + order.getInstrument() + " " + orderEvent);
        orderEventPublisher.onNext(orderEvent);
    }
}
