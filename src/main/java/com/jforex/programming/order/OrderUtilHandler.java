package com.jforex.programming.order;

import static com.jforex.programming.order.event.OrderEventTypeSets.finishEventTypes;
import static com.jforex.programming.order.event.OrderEventTypeSets.rejectEventTypes;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventTypeData;

import rx.Observable;

public class OrderUtilHandler {

    private final OrderCallExecutor orderCallExecutor;
    private final OrderEventGateway orderEventGateway;

    public OrderUtilHandler(final OrderCallExecutor orderCallExecutor,
                            final OrderEventGateway orderEventGateway) {
        this.orderCallExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public Observable<OrderEvent> callObservable(final OrderCallCommand command) {
        final OrderEventTypeData orderEventTypeData = command.orderEventTypeData();
        return orderCallExecutor
                .callObservable(command.callable())
                .doOnNext(order -> registerOrder(order, orderEventTypeData.callReason()))
                .flatMap(order -> gatewayObservable(order, orderEventTypeData))
                .doOnSubscribe(command::logOnSubscribe)
                .doOnError(command::logOnError)
                .doOnCompleted(command::logOnCompleted);
    }

    public Observable<OrderEvent> rejectAsErrorObservable(final OrderEvent orderEvent) {
        return rejectEventTypes.contains(orderEvent.type())
                ? Observable.error(new OrderCallRejectException("", orderEvent))
                : Observable.just(orderEvent);
    }

    private final void registerOrder(final IOrder order,
                                     final OrderCallReason orderCallReason) {
        final OrderCallRequest orderCallRequest = new OrderCallRequest(order, orderCallReason);
        orderEventGateway.registerOrderCallRequest(orderCallRequest);
    }

    private final Observable<OrderEvent> gatewayObservable(final IOrder order,
                                                           final OrderEventTypeData orderEventTypeData) {
        return orderEventGateway
                .observable()
                .filter(orderEvent -> orderEvent.order().equals(order))
                .filter(orderEvent -> orderEventTypeData.all().contains(orderEvent.type()))
                .takeUntil(orderEvent -> finishEventTypes.contains(orderEvent.type()));
    }
}
