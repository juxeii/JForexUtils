package com.jforex.programming.order;

import static com.jforex.programming.order.event.OrderEventTypeSets.rejectEvents;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.TaskExecutor;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;

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
        return taskExecutor
            .onStrategyThread(command.callable())
            .doOnSubscribe(command::logOnSubscribe)
            .doOnNext(order -> registerOrder(order, command.callReason()))
            .flatMap(order -> gatewayObservable(order, command))
            .doOnError(command::logOnError)
            .doOnCompleted(command::logOnCompleted);
    }

    private final void registerOrder(final IOrder order,
                                     final OrderCallReason orderCallReason) {
        final OrderCallRequest orderCallRequest = new OrderCallRequest(order, orderCallReason);
        orderEventGateway.registerOrderCallRequest(orderCallRequest);
    }

    private final Observable<OrderEvent> gatewayObservable(final IOrder order,
                                                           final OrderCallCommand command) {
        return orderEventGateway
            .observable()
            .filter(orderEvent -> orderEvent.order().equals(order))
            .filter(orderEvent -> command.allEventTypes().contains(orderEvent.type()))
            .flatMap(this::rejectAsErrorObservable)
            .takeUntil(orderEvent -> command.doneEventTypes().contains(orderEvent.type()));
    }

    private Observable<OrderEvent> rejectAsErrorObservable(final OrderEvent orderEvent) {
        return rejectEvents.contains(orderEvent.type())
                ? Observable.error(new OrderCallRejectException("", orderEvent))
                : Observable.just(orderEvent);
    }
}
