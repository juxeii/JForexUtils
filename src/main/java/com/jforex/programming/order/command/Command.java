package com.jforex.programming.order.command;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import io.reactivex.functions.Action;

public abstract class Command {

    private final Action startAction;
    private final Action completeAction;
    private final Consumer<Throwable> errorAction;
    private final Consumer<OrderEvent> eventAction;
    private final Callable<IOrder> callable;
    private final OrderCallReason callReason;
    private final OrderEventTypeData orderEventTypeData;
    private final int noOfRetries;
    private final long retryDelayInMillis;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    public Command(final CommonBuilder<?> builder) {
        callable = builder.callable;
        callReason = builder.callReason;
        orderEventTypeData = builder.orderEventTypeData;
        startAction = builder.startAction;
        completeAction = builder.completedAction;
        eventAction = builder.eventAction;
        errorAction = builder.errorAction;
        noOfRetries = builder.noOfRetries;
        retryDelayInMillis = builder.retryDelayInMillis;
        eventHandlerForType = builder.eventHandlerForType;
    }

    public Action startAction() {
        return startAction;
    }

    public Action completedAction() {
        return completeAction;
    }

    public Consumer<OrderEvent> eventAction() {
        return eventAction;
    }

    public Consumer<Throwable> errorAction() {
        return errorAction;
    }

    public Callable<IOrder> callable() {
        return callable;
    }

    public OrderCallReason callReason() {
        return callReason;
    }

    public boolean isEventTypeForCommand(final OrderEventType orderEventType) {
        return orderEventTypeData
            .allEventTypes()
            .contains(orderEventType);
    }

    private boolean isDoneEventType(final OrderEventType orderEventType) {
        return orderEventTypeData
            .doneEventTypes()
            .contains(orderEventType);
    }

    public boolean isRejectEventType(final OrderEventType orderEventType) {
        return orderEventTypeData
            .rejectEventTypes()
            .contains(orderEventType);
    }

    public boolean isFinishEventType(final OrderEventType orderEventType) {
        return isDoneEventType(orderEventType) || isRejectEventType(orderEventType);
    }

    public int noOfRetries() {
        return noOfRetries;
    }

    public long retryDelayInMillis() {
        return retryDelayInMillis;
    }

    public Map<OrderEventType, Consumer<IOrder>> eventHandlerForType() {
        return eventHandlerForType;
    }
}
