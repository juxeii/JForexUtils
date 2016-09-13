package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Maps;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.CommonOption;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import io.reactivex.functions.Action;

@SuppressWarnings("unchecked")
public class CommonBuilder<T extends CommonOption<T>> {

    protected Action startAction = () -> {};
    protected Action completedAction = () -> {};
    protected Consumer<OrderEvent> eventAction = o -> {};
    protected Consumer<Throwable> errorAction = o -> {};
    protected int noOfRetries;
    protected long retryDelayInMillis;
    protected Callable<IOrder> callable;
    protected OrderCallReason callReason;
    protected OrderEventTypeData orderEventTypeData;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = Maps.newEnumMap(OrderEventType.class);

    public T doOnStart(final Action startAction) {
        this.startAction = checkNotNull(startAction);
        return (T) this;
    }

    public T doOnComplete(final Action completedAction) {
        this.completedAction = checkNotNull(completedAction);
        return (T) this;
    }

    public T doOnError(final Consumer<Throwable> errorAction) {
        this.errorAction = checkNotNull(errorAction);
        return (T) this;
    }

    public T doOnOrderEvent(final Consumer<OrderEvent> eventAction) {
        this.eventAction = checkNotNull(eventAction);
        return (T) this;
    }

    public T retry(final int noOfRetries,
                   final long retryDelayInMillis) {
        this.noOfRetries = noOfRetries;
        this.retryDelayInMillis = retryDelayInMillis;
        return (T) this;
    }

    protected T registerTypeHandler(final OrderEventType type,
                                    final Consumer<IOrder> handler) {
        eventHandlerForType.put(type, checkNotNull(handler));
        return (T) this;
    }
}
