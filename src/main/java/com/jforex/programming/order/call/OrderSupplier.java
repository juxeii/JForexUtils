package com.jforex.programming.order.call;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

@FunctionalInterface
public interface OrderSupplier {

    public IOrder get() throws JFException;
}
