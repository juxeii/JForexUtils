package com.jforex.programming.order;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.TaskExecutor;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.command.CommandRetry;
import com.jforex.programming.order.command.CommonCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeSets;

import rx.Observable;

public class OrderUtilHandler {

    private final TaskExecutor taskExecutor;
    private final OrderEventGateway orderEventGateway;

    public OrderUtilHandler(final TaskExecutor orderCallExecutor,
                            final OrderEventGateway orderEventGateway) {
        this.taskExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public Observable<OrderEvent> callObservable(final CommonCommand command) {
        final Observable<OrderEvent> observable = taskExecutor
            .onStrategyThread(command.callable())
            .doOnNext(order -> registerOrder(order, command.callReason()))
            .flatMap(order -> gatewayObservable(order, command))
            .doOnNext(orderEvent -> callEventHandler(orderEvent, command.eventHandlerForType()))
            .doOnNext(command.eventAction()::accept);

        return decorateRetry(observable, command.noOfRetries(), command.delayInMillis());
    }

    private final void registerOrder(final IOrder order,
                                     final OrderCallReason orderCallReason) {
        final OrderCallRequest orderCallRequest = new OrderCallRequest(order, orderCallReason);
        orderEventGateway.registerOrderCallRequest(orderCallRequest);
    }

    private final Observable<OrderEvent> gatewayObservable(final IOrder order,
                                                           final CommonCommand command) {
        return orderEventGateway
            .observable()
            .filter(orderEvent -> orderEvent.order().equals(order))
            .filter(command::isEventForCommand)
            .takeUntil(command::isFinishEvent);
    }

    private final void callEventHandler(final OrderEvent orderEvent,
                                        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType) {
        final OrderEventType type = orderEvent.type();
        if (eventHandlerForType.containsKey(type))
            eventHandlerForType
                .get(type)
                .accept(orderEvent.order());
    }

    private final Observable<OrderEvent> decorateRetry(final Observable<OrderEvent> observable,
                                                       final int noOfRetries,
                                                       final long delayInMillis) {
        if (noOfRetries > 0) {
            final CommandRetry orderProcessRetry = new CommandRetry(noOfRetries, delayInMillis);
            return observable
                .flatMap(this::rejectAsErrorObservable)
                .retryWhen(orderProcessRetry::retryOnRejectObservable);
        }
        return observable;
    }

    private Observable<OrderEvent> rejectAsErrorObservable(final OrderEvent orderEvent) {
        return OrderEventTypeSets.rejectEvents.contains(orderEvent.type())
                ? Observable.error(new OrderCallRejectException("Reject event", orderEvent))
                : Observable.just(orderEvent);
    }
}
