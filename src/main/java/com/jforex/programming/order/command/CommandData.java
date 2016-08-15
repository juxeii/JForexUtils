package com.jforex.programming.order.command;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public interface CommandData {

    public Callable<IOrder> callable();

    public OrderCallReason callReason();

    public OrderEventTypeData orderEventTypeData();
}
