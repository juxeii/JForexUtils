package com.jforex.programming.order.command;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTypeData;

public abstract class OrderCallCommand {

    private final Callable<IOrder> callable;
    private final OrderEventTypeData orderEventTypeData;

    protected static final Logger logger = LogManager.getLogger(OrderCallCommand.class);

    public OrderCallCommand(final Callable<IOrder> callable,
                            final OrderEventTypeData orderEventTypeData) {
        this.callable = callable;
        this.orderEventTypeData = orderEventTypeData;
    }

    public final Callable<IOrder> callable() {
        return callable;
    }

    public final OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

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
