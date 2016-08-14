package com.jforex.programming.order.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;

public abstract class OrderChangeCommand<T> implements OrderCallCommand {

    protected IOrder orderToChange;
    protected T currentValue;
    protected T newValue;
    protected String valueName;
    private String commonLog;

    private static final Logger logger = LogManager.getLogger(OrderChangeCommand.class);

    protected void createCommonLog() {
        commonLog = valueName + " from " + currentValue + " to " + newValue + " for order "
                + orderToChange.getLabel() + " and instrument " + orderToChange.getInstrument();
    }

    public abstract boolean filter();

    @Override
    public void logOnSubscribe() {
        logger.info("Start to change " + commonLog);
    }

    @Override
    public void logOnError(final Throwable t) {
        logger.error("Failed to change " + commonLog + "!Excpetion: " + t.getMessage());
    }

    @Override
    public void logOnCompleted() {
        logger.info("Changed " + commonLog);
    }
}
