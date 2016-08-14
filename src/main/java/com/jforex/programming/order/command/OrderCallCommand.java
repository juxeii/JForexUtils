package com.jforex.programming.order.command;

import java.util.Set;
import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public interface OrderCallCommand {

    public Callable<IOrder> callable();

    public OrderCallReason callReason();

    public Set<OrderEventType> allEventTypes();

    public Set<OrderEventType> doneEventTypes();

    public Set<OrderEventType> rejectEventTypes();

    public void logOnSubscribe();

    public void logOnError(final Throwable t);

    public void logOnCompleted();
}
