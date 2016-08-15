package com.jforex.programming.order.command;

public interface OrderChangeCommandData<T> extends CommandData {

    public boolean isValueNotSet();
}
