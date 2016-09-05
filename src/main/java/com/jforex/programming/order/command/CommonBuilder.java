package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Maps;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.order.process.option.CommonOption;

import rx.Completable;
import rx.functions.Action0;

@SuppressWarnings("unchecked")
public class CommonBuilder<T extends CommonOption<T>> {

    protected Action0 completedAction = () -> {};
    protected Consumer<OrderEvent> eventAction = o -> {};
    protected Consumer<Throwable> errorAction = o -> {};
    protected int noOfRetries;
    protected long retryDelayInMillis;
    protected Callable<IOrder> callable;
    protected OrderCallReason callReason;
    protected OrderEventTypeData orderEventTypeData;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = Maps.newEnumMap(OrderEventType.class);
    protected Function<? extends CommonCommand, Completable> startFunction;

    public T doOnCompleted(final Action0 completedAction) {
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

    public T retry(final int noOfRetries, final long retryDelayInMillis) {
        this.noOfRetries = noOfRetries;
        this.retryDelayInMillis = retryDelayInMillis;
        return (T) this;
    }
}
