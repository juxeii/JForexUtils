package com.jforex.programming.order.command;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTypeData;

public abstract class OrderCallCommand {

    protected Callable<IOrder> callable;
    protected OrderEventTypeData orderEventTypeData;

    protected static final Logger logger = LogManager.getLogger(OrderCallCommand.class);

    public Callable<IOrder> callable() {
        return callable;
    }

    public OrderEventTypeData orderEventTypeData() {
        return orderEventTypeData;
    }

    public void logOnSubscribe() {
        logger.info(subscribeLog());
    }

    public void logOnError(final Throwable t) {
        logger.error(errorLog(t));
    }

    public void logOnCompleted() {
        logger.info(completedLog());
    }

    protected abstract String subscribeLog();

    protected abstract String errorLog(final Throwable t);

    protected abstract String completedLog();
}
