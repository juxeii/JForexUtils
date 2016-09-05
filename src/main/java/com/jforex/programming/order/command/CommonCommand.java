package com.jforex.programming.order.command;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import rx.Completable;
import rx.functions.Action0;

public class CommonCommand implements OrderUtilCommand {

    private final Action0 startAction;
    private final Action0 completedAction;
    private final Consumer<Throwable> errorAction;
    private final Consumer<OrderEvent> eventAction;
    private final Callable<IOrder> callable;
    private final OrderCallReason callReason;
    private final OrderEventTypeData orderEventTypeData;
    private final int noOfRetries;
    private final long retryDelayInMillis;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;
    private final Function<CommonCommand, Completable> startFunction;

    protected static final Logger logger = LogManager.getLogger(CommonCommand.class);

    @SuppressWarnings("unchecked")
    protected CommonCommand(final CommonBuilder<?> builder) {
        callable = builder.callable;
        callReason = builder.callReason;
        orderEventTypeData = builder.orderEventTypeData;
        startAction = builder.startAction;
        completedAction = builder.completedAction;
        eventAction = builder.eventAction;
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        retryDelayInMillis = builder.retryDelayInMillis;
        eventHandlerForType = builder.eventHandlerForType;
        startFunction = (Function<CommonCommand, Completable>) builder.startFunction;
    }

    @Override
    public Completable completable() {
        return startFunction.apply(this);
    }

    public Action0 startAction() {
        return startAction;
    }

    public Action0 completedAction() {
        return completedAction;
    }

    public Consumer<OrderEvent> eventAction() {
        return eventAction;
    }

    public Consumer<Throwable> errorAction() {
        return errorAction;
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

    public final int noOfRetries() {
        return noOfRetries;
    }

    public final long retryDelayInMillis() {
        return retryDelayInMillis;
    }

    public final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType() {
        return eventHandlerForType;
    }
}
