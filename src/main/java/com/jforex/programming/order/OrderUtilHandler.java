package com.jforex.programming.order;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.order.event.OrderEventTypeDataFactory;

import io.reactivex.Observable;

public class OrderUtilHandler {

    private final OrderEventGateway orderEventGateway;
    private final OrderEventTypeDataFactory orderEventTypeDataFactory;

    public OrderUtilHandler(final OrderEventGateway orderEventGateway,
                            final OrderEventTypeDataFactory orderEventTypeDataFactory) {
        this.orderEventGateway = orderEventGateway;
        this.orderEventTypeDataFactory = orderEventTypeDataFactory;
    }

    public Observable<OrderEvent> callObservable(final IOrder orderOfCall,
                                                 final OrderCallReason callReason) {
        return Observable
            .just(orderOfCall)
            .doOnSubscribe(d -> registerOrder(orderOfCall, callReason))
            .map(order -> orderEventTypeDataFactory.forCallReason(callReason))
            .flatMap(type -> gatewayObservable(orderOfCall, type));
    }

    private final void registerOrder(final IOrder order,
                                     final OrderCallReason callReason) {
        final OrderCallRequest orderCallRequest = new OrderCallRequest(order, callReason);
        orderEventGateway.registerOrderCallRequest(orderCallRequest);
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
