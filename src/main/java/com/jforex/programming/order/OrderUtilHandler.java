package com.jforex.programming.order;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.TaskExecutor;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.command.CommandData;
import com.jforex.programming.order.command.CommandRetry;
import com.jforex.programming.order.command.Command;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Observable;

public class OrderUtilHandler {

    private final TaskExecutor taskExecutor;
    private final OrderEventGateway orderEventGateway;

    public OrderUtilHandler(final TaskExecutor orderCallExecutor,
                            final OrderEventGateway orderEventGateway) {
        this.taskExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public Observable<OrderEvent> callObservable(final Command command) {
        final CommandData commandData = command.data();
        final Observable<OrderEvent> observable = taskExecutor
            .onStrategyThread(commandData.callable())
            .doOnSubscribe(d -> commandData.startAction().run())
            .doOnNext(order -> registerOrder(order, commandData.callReason()))
            .flatMap(order -> gatewayObservable(order, commandData))
            .doOnNext(orderEvent -> callEventHandler(orderEvent, commandData.eventHandlerForType()))
            .doOnNext(commandData.eventAction()::accept);

        return decorateObservableWithRetry(commandData, observable)
            .doOnError(commandData.errorAction()::accept)
            .doOnComplete(commandData.completedAction()::run);
    }

    private final void registerOrder(final IOrder order,
                                     final OrderCallReason orderCallReason) {
        final OrderCallRequest orderCallRequest = new OrderCallRequest(order, orderCallReason);
        orderEventGateway.registerOrderCallRequest(orderCallRequest);
    }

    private final Observable<OrderEvent> gatewayObservable(final IOrder order,
                                                           final CommandData commandData) {
        return orderEventGateway
            .observable()
            .filter(orderEvent -> orderEvent.order().equals(order))
            .filter(orderEvent -> commandData.isEventTypeForCommand(orderEvent.type()))
            .takeUntil((final OrderEvent orderEvent) -> commandData.isFinishEventType(orderEvent.type()));
    }

    private final void callEventHandler(final OrderEvent orderEvent,
                                        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType) {
        final OrderEventType type = orderEvent.type();
        if (eventHandlerForType.containsKey(type))
            eventHandlerForType
                .get(type)
                .accept(orderEvent.order());
    }

    private final Observable<OrderEvent> decorateObservableWithRetry(final CommandData command,
                                                                     final Observable<OrderEvent> observable) {
        final int noOfRetries = command.noOfRetries();
        if (noOfRetries > 0) {
            final CommandRetry orderProcessRetry = new CommandRetry(noOfRetries,
                                                                    command.retryDelayInMillis());
            return observable
                .flatMap(orderEvent -> rejectAsErrorObservable(command, orderEvent))
                .retryWhen(orderProcessRetry::retryOnRejectObservable);
        }
        return observable;
    }

    private Observable<OrderEvent> rejectAsErrorObservable(final CommandData command,
                                                           final OrderEvent orderEvent) {
        return command.isRejectEventType(orderEvent.type())
                ? Observable.error(new OrderCallRejectException("Reject event", orderEvent))
                : Observable.just(orderEvent);
    }
}
