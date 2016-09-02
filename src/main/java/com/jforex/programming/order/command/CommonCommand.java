package com.jforex.programming.order.command;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import rx.Observable;
import rx.functions.Action0;

public class CommonCommand implements OrderUtilCommand {

    private final Callable<IOrder> callable;
    private final OrderCallReason callReason;
    private final OrderEventTypeData orderEventTypeData;
    protected Observable<OrderEvent> observable;
    private final Action0 completedAction;
    private final Consumer<OrderEvent> eventAction;
    private final Consumer<Throwable> errorAction;
    private final int noOfRetries;
    private final long delayInMillis;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    protected static final Logger logger = LogManager.getLogger(CommonCommand.class);

    protected CommonCommand(final CommonBuilder<?> builder) {
        callable = builder.callable;
        callReason = builder.callReason;
        orderEventTypeData = builder.orderEventTypeData;
        completedAction = builder.completedAction;
        eventAction = builder.eventAction;
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        delayInMillis = builder.delayInMillis;
        eventHandlerForType = builder.eventHandlerForType;
    }

    @Override
    public final void start() {
        observable
            .subscribe(eventAction::accept,
                       errorAction::accept,
                       completedAction::call);
    }

    public final Callable<IOrder> callable() {
        return callable;
    }

    public final OrderCallReason callReason() {
        return callReason;
    }

    public final boolean isEventForCommand(final OrderEvent orderEvent) {
        return orderEventTypeData
            .allEventTypes()
            .contains(orderEvent.type());
    }

    private final boolean isDoneEvent(final OrderEvent orderEvent) {
        return orderEventTypeData
            .doneEventTypes()
            .contains(orderEvent.type());
    }

    private final boolean isRejectEvent(final OrderEvent orderEvent) {
        return orderEventTypeData
            .rejectEventTypes()
            .contains(orderEvent.type());
    }

    public final boolean isFinishEvent(final OrderEvent orderEvent) {
        return isDoneEvent(orderEvent) || isRejectEvent(orderEvent);
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

    protected Observable<OrderEvent> changeObservable(final Observable<OrderEvent> observable,
                                                      final IOrder order,
                                                      final String commonLog) {
        final String logMsg = commonLog + " for order " + order.getLabel()
                + " and instrument " + order.getInstrument();
        return observable
            .doOnSubscribe(() -> logger.info("Start to change " + logMsg))
            .doOnError(e -> logger.error("Failed to change " + logMsg + "!Excpetion: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Changed " + logMsg));
    }
}
