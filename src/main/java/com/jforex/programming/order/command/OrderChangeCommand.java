package com.jforex.programming.order.command;

public interface OrderChangeCommand<T> extends OrderCallCommand {

    public boolean filter();
}
