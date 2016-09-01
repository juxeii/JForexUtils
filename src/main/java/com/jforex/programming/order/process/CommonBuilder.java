package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Maps;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.CommonOption;

@SuppressWarnings("unchecked")
public class CommonBuilder<T> implements CommonOption<T> {

    protected Consumer<Throwable> errorAction = o -> {};
    protected int noOfRetries;
    protected long delayInMillis;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = Maps.newEnumMap(OrderEventType.class);

    @Override
    public T onError(final Consumer<Throwable> errorAction) {
        this.errorAction = checkNotNull(errorAction);
        return (T) this;
    }

    @Override
    public T doRetries(final int noOfRetries, final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
        return (T) this;
    }
}
