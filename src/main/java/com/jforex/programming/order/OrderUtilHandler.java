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

import io.reactivex.Flowable;

public class OrderUtilHandler {

    private final TaskExecutor taskExecutor;
    private final OrderEventGateway orderEventGateway;

    public OrderUtilHandler(final TaskExecutor orderCallExecutor,
                            final OrderEventGateway orderEventGateway) {
        this.taskExecutor = orderCallExecutor;
        this.orderEventGateway = orderEventGateway;
    }

    public Flowable<OrderEvent> callObservable(final CommonCommand command) {
        final Flowable<OrderEvent> observable = taskExecutor
            .onStrategyThread(command.callable())
            .doOnSubscribe(d -> command.startAction().run())
            .doOnNext(order -> registerOrder(order, command.callReason()))
            .flatMap(order -> gatewayObservable(order, command))
            .doOnNext(orderEvent -> callEventHandler(orderEvent, command.eventHandlerForType()))
            .doOnNext(command.eventAction()::accept);

        return decorateObservableWithRetry(command, observable)
            .doOnError(command.errorAction()::accept)
            .doOnComplete(command.completedAction()::run);
    }

    private final void registerOrder(final IOrder order,
                                     final OrderCallReason orderCallReason) {
        final OrderCallRequest orderCallRequest = new OrderCallRequest(order, orderCallReason);
        orderEventGateway.registerOrderCallRequest(orderCallRequest);
    }

    private final Flowable<OrderEvent> gatewayObservable(final IOrder order,
                                                         final CommonCommand command) {
        return orderEventGateway
            .observable()
            .filter(orderEvent -> orderEvent.order().equals(order))
            .filter(orderEvent -> command.isEventTypeForCommand(orderEvent.type()))
            .takeUntil((final OrderEvent orderEvent) -> command.isFinishEventType(orderEvent.type()));
    }

    private final void callEventHandler(final OrderEvent orderEvent,
                                        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType) {
        final OrderEventType type = orderEvent.type();
        if (eventHandlerForType.containsKey(type))
            eventHandlerForType
                .get(type)
                .accept(orderEvent.order());
    }

    private final Flowable<OrderEvent> decorateObservableWithRetry(final CommonCommand command,
                                                                   final Flowable<OrderEvent> observable) {
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

    private Flowable<OrderEvent> rejectAsErrorObservable(final CommonCommand command,
                                                         final OrderEvent orderEvent) {
        return command.isRejectEventType(orderEvent.type())
                ? Flowable.error(new OrderCallRejectException("Reject event", orderEvent))
                : Flowable.just(orderEvent);
    }
}
