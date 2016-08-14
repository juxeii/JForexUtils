package com.jforex.programming.order.command;

import com.dukascopy.api.IOrder;

public abstract class OrderChangeCommand<T> extends OrderCallCommand {

    protected IOrder orderToChange;
    protected T currentValue;
    protected T newValue;
    protected String valueName;
    private String commonLog;

    protected void createCommonLog() {
        commonLog = valueName + " from " + currentValue + " to " + newValue + " for order "
                + orderToChange.getLabel() + " and instrument " + orderToChange.getInstrument();
    }

    public abstract boolean filter();

    @Override
    protected final String subscribeLog() {
        return "Start to change " + commonLog;
    }

    @Override
    protected final String errorLog(final Throwable t) {
        return "Failed to change " + commonLog + "!Excpetion: " + t.getMessage();
    }

    @Override
    protected final String completedLog() {
        return "Changed " + commonLog;
    }
}
