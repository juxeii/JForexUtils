package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Maps;
import com.jforex.programming.order.event.OrderEventType;

@SuppressWarnings("unchecked")
public abstract class CommonBuilder<T extends CommonBuilder<T>> {

    protected Consumer<Throwable> errorAction = o -> {};
    protected int noOfRetries;
    protected long delayInMillis;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = Maps.newEnumMap(OrderEventType.class);

    public T onError(final Consumer<Throwable> errorAction) {
        this.errorAction = checkNotNull(errorAction);
        return (T) this;
    }

    public T doRetries(final int noOfRetries,
                       final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
        return (T) this;
    }
}
