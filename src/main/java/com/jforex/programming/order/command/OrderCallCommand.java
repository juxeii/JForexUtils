package com.jforex.programming.order.command;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;

public abstract class OrderCallCommand {

    private final Callable<IOrder> callable;

    protected static final Logger logger = LogManager.getLogger(OrderCallCommand.class);

    public OrderCallCommand(final Callable<IOrder> callable) {
        this.callable = callable;
    }

    public final Callable<IOrder> callable() {
        return callable;
    }

    public abstract OrderCallReason callReason();

    public abstract Set<OrderEventType> allEventTypes();

    public abstract Set<OrderEventType> doneEventTypes();

    public abstract Set<OrderEventType> rejectEventTypes();

    public final void logOnSubscribe() {
        logger.info(subscribeLog());
    }

    public final void logOnError(final Throwable t) {
        logger.error(errorLog(t));
    }

    public final void logOnCompleted() {
        logger.info(completedLog());
    }

    protected abstract String subscribeLog();

    protected abstract String errorLog(final Throwable t);

    protected abstract String completedLog();
}
