package com.jforex.programming.order.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.JFHotSubject;
import com.jforex.programming.order.call.OrderCallRequest;

import io.reactivex.Observable;

public class OrderEventGateway {

    private final JFHotSubject<OrderEvent> orderEventPublisher = new JFHotSubject<>();
    private final OrderEventFactory messageToOrderEvent;

    private static final Logger logger = LogManager.getLogger(OrderEventGateway.class);

    public OrderEventGateway(final Observable<IMessage> messageObservable,
                             final OrderEventFactory orderEventMapper) {
        this.messageToOrderEvent = orderEventMapper;

        messageObservable
            .filter(message -> message.getOrder() != null)
            .subscribe(this::onOrderMessage);
    }

    public Observable<OrderEvent> observable() {
        return orderEventPublisher.observable();
    }

    public void registerOrderCallRequest(final OrderCallRequest orderCallRequest) {
        messageToOrderEvent.registerOrderCallRequest(orderCallRequest);
    }

    private void onOrderMessage(final IMessage message) {
        final OrderEvent orderEvent = messageToOrderEvent.fromMessage(message);
        final IOrder order = orderEvent.order();
        logger.debug("Received order event with label " + order.getLabel()
                + " for " + order.getInstrument() + " " + orderEvent);
        orderEventPublisher.onNext(orderEvent);
    }
}
