package com.jforex.programming.order.command;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;

public interface OrderCallCommand {

    public Callable<IOrder> callable();

    public OrderCallReason callReason();

    public boolean isEventForCommand(OrderEvent orderEvent);

    public boolean isDoneEvent(OrderEvent orderEvent);

    public boolean isRejectEvent(OrderEvent orderEvent);
}
