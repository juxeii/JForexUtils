package com.jforex.programming.order;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;

import io.reactivex.Observable;

public class OrderUtilHandler {

    private final OrderEventGateway orderEventGateway;
    private final OrderTaskDataFactory orderTaskDataFactory;

    public OrderUtilHandler(final OrderEventGateway orderEventGateway,
                            final OrderTaskDataFactory orderTaskDataFactory) {
        this.orderEventGateway = orderEventGateway;
        this.orderTaskDataFactory = orderTaskDataFactory;
    }

    public Observable<OrderEvent> callObservable(final IOrder order,
                                                 final OrderCallReason callReason) {
        return Observable
            .just(orderTaskDataFactory.forCallReason(order, callReason))
            .doOnNext(this::registerOrder)
            .flatMap(this::gatewayObservable);
    }

    private final void registerOrder(final OrderTaskData taskData) {
        final OrderCallRequest orderCallRequest = new OrderCallRequest(taskData.order(), taskData.callReason());
        orderEventGateway.registerOrderCallRequest(orderCallRequest);
    }

    private final Observable<OrderEvent> gatewayObservable(final OrderTaskData taskData) {
        return orderEventGateway
            .observable()
            .filter(orderEvent -> orderEvent.order().equals(taskData.order()))
            .filter(orderEvent -> taskData.isEventTypeForTask(orderEvent.type()))
            .takeUntil((final OrderEvent orderEvent) -> taskData.isFinishEventType(orderEvent.type()));
    }
}
