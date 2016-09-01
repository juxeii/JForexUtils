package com.jforex.programming.order.process;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Maps;
import com.jforex.programming.order.event.OrderEventType;

public class CommonBuilder {

    protected Consumer<Throwable> errorAction = o -> {};
    protected int noOfRetries;
    protected long delayInMillis;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = Maps.newEnumMap(OrderEventType.class);
}
