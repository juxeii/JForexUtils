package com.jforex.programming.order;

import static com.jforex.programming.order.event.OrderEventTypeSets.finishEvents;
import static com.jforex.programming.order.event.OrderEventTypeSets.rejectEvents;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.TaskExecutor;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventTypeData;

import rx.Observable;

public class OrderUtilHandler {

    private final TaskExecutor taskExecutor;
    private final OrderEventGateway orderEventGateway;

    public OrderUtilHandler(final TaskExecutor orderCallExecutor,
                            final OrderEventGateway orderEventGateway) {
        this.taskExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public Observable<OrderEvent> callObservable(final OrderCallCommand command) {
        final OrderEventTypeData orderEventTypeData = command.orderEventTypeData();
        return taskExecutor
            .onStrategyThreadIfNeeded(command.callable())
            .doOnSubscribe(command::logOnSubscribe)
            .doOnNext(order -> registerOrder(order, orderEventTypeData.callReason()))
            .flatMap(order -> gatewayObservable(order, orderEventTypeData))
            .doOnError(command::logOnError)
            .doOnCompleted(command::logOnCompleted);
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
            .flatMap(this::rejectAsErrorObservable)
            .takeUntil(orderEvent -> finishEvents.contains(orderEvent.type()));
    }

    private Observable<OrderEvent> rejectAsErrorObservable(final OrderEvent orderEvent) {
        return rejectEvents.contains(orderEvent.type())
                ? Observable.error(new OrderCallRejectException("", orderEvent))
                : Observable.just(orderEvent);
    }
}
