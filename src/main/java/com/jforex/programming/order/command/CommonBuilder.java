package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Maps;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.order.process.option.CommonOption;

import rx.Completable;
import rx.functions.Action0;

@SuppressWarnings("unchecked")
public abstract class CommonBuilder<T extends CommonOption<T>> {

    protected OrderUtilHandler orderUtilHandler;
    protected OrderUtil orderUtil;
    protected Callable<IOrder> callable;
    protected OrderCallReason callReason;
    protected OrderEventTypeData orderEventTypeData;
    protected Action0 completedAction = () -> {};
    protected Consumer<OrderEvent> eventAction = o -> {};
    protected Consumer<Throwable> errorAction = o -> {};
    protected int noOfRetries;
    protected long delayInMillis;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = Maps.newEnumMap(OrderEventType.class);
    protected Function<? extends CommonCommand, Completable> startFunction;

    protected static final Logger logger = LogManager.getLogger(CommonBuilder.class);

    public T onCompleted(final Action0 completedAction) {
        this.completedAction = checkNotNull(completedAction);
        return (T) this;
    }

    public T onEvent(final Consumer<OrderEvent> eventAction) {
        this.eventAction = checkNotNull(eventAction);
        return (T) this;
    }

    public T onError(final Consumer<Throwable> errorAction) {
        this.errorAction = checkNotNull(errorAction);
        return (T) this;
    }

    public T doRetries(final int noOfRetries, final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
        return (T) this;
    }
}
