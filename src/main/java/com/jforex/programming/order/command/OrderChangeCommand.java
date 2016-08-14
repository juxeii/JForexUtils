package com.jforex.programming.order.command;

import com.dukascopy.api.IOrder;

public interface OrderChangeCommand<T> extends OrderCallCommand {

    public IOrder order();

    public boolean filter();

    public T currentValue();

    public T newValue();

    public String valueName();
}
