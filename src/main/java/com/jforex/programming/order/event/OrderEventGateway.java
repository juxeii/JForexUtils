package com.jforex.programming.order.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.jforex.programming.rx.JFHotPublisher;

import io.reactivex.Observable;

public class OrderEventGateway {

    private final JFHotPublisher<OrderEvent> orderEventPublisher = new JFHotPublisher<>();
    private final OrderEventFactory orderEventFactory;

    private static final Logger logger = LogManager.getLogger(OrderEventGateway.class);

    public OrderEventGateway(final Observable<IMessage> messageObservable,
                             final OrderEventFactory orderEventFactory) {
        this.orderEventFactory = orderEventFactory;

        messageObservable
            .filter(message -> message.getOrder() != null)
            .subscribe(this::onOrderMessage);
    }

    private void onOrderMessage(final IMessage message) {
        final OrderEvent orderEvent = orderEventFactory.fromMessage(message);
        final IOrder order = orderEvent.order();
        logger.debug("Received order event with label " + order.getLabel()
                + " for " + order.getInstrument() + " " + orderEvent);
        orderEventPublisher.onNext(orderEvent);
    }

    public Observable<OrderEvent> observable() {
        return orderEventPublisher.observable();
    }

    public void importOrder(final IOrder order) {
        final OrderEvent orderEvent = new OrderEvent(order,
                                                     OrderEventType.SUBMIT_OK,
                                                     true);
        logger.debug("Importing order " + order.getLabel() + " for " + order.getInstrument());
        orderEventPublisher.onNext(orderEvent);
    }
}
