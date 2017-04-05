package com.jforex.programming.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.order.event.OrderEventTypeDataFactory;
import com.jforex.programming.rx.JFHotPublisher;

import io.reactivex.Observable;

public class OrderUtilHandler {

    private final OrderEventGateway orderEventGateway;
    private final OrderEventTypeDataFactory orderEventTypeDataFactory;
    private final JFHotPublisher<OrderCallRequest> callRequestPublisher;

    private final static Logger logger = LogManager.getLogger(OrderUtilHandler.class);

    public OrderUtilHandler(final OrderEventGateway orderEventGateway,
                            final OrderEventTypeDataFactory orderEventTypeDataFactory,
                            final JFHotPublisher<OrderCallRequest> callRequestPublisher) {
        this.orderEventGateway = orderEventGateway;
        this.orderEventTypeDataFactory = orderEventTypeDataFactory;
        this.callRequestPublisher = callRequestPublisher;
    }

    public Observable<OrderEvent> callObservable(final IOrder orderOfCall,
                                                 final OrderCallReason callReason) {
        logger.info("Called callObservable");
        return Observable
            .just(orderOfCall)
            .doOnSubscribe(d -> callRequestPublisher.onNext(new OrderCallRequest(orderOfCall, callReason)))
            .map(order -> orderEventTypeDataFactory.forCallReason(callReason))
            .flatMap(type -> gatewayObservable(orderOfCall, type));
    }

    private final Observable<OrderEvent> gatewayObservable(final IOrder order,
                                                           final OrderEventTypeData typeData) {
        return orderEventGateway
            .observable()
            .filter(orderEvent -> orderEvent.order().equals(order))
            .filter(orderEvent -> typeData.allEventTypes().contains(orderEvent.type()))
            .takeUntil((final OrderEvent orderEvent) -> typeData.finishEventTypes().contains(orderEvent.type()));
    }
}
