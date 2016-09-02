package com.jforex.programming.order.command;

import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeSets;

import rx.Observable;
import rx.functions.Action0;

public class CommonCommand implements OrderUtilCommand {

    private final Observable<OrderEvent> observable;
    private final Action0 completedAction;
    private final Consumer<OrderEvent> eventAction;
    private final Consumer<Throwable> errorAction;
    private final int noOfRetries;
    private final long delayInMillis;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    protected static final Logger logger = LogManager.getLogger(CommonCommand.class);

    protected CommonCommand(final CommonBuilder<?> builder) {
        observable = builder.observable;
        completedAction = builder.completedAction;
        eventAction = builder.eventAction;
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
        eventHandlerForType = builder.eventHandlerForType;
    }

    @Override
    public final void start() {
        evaluateRetry(observable.doOnNext(this::callEventHandler))
            .subscribe(eventAction::accept,
                       errorAction::accept,
                       completedAction::call);
    }

    public final Action0 completedAction() {
        return completedAction;
    }

    public final Consumer<OrderEvent> eventAction() {
        return eventAction;
    }

    public final Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public final int noOfRetries() {
        return noOfRetries;
    }

    public final long delayInMillis() {
        return delayInMillis;
    }

    public final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType() {
        return eventHandlerForType;
    }

    private final void callEventHandler(final OrderEvent orderEvent) {
        final OrderEventType type = orderEvent.type();
        if (eventHandlerForType.containsKey(type))
            eventHandlerForType
                .get(type)
                .accept(orderEvent.order());
    }

    private final Observable<OrderEvent> evaluateRetry(final Observable<OrderEvent> observable) {
        if (noOfRetries() > 0) {
            final CommandRetry orderProcessRetry = new CommandRetry(noOfRetries(), delayInMillis());
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
