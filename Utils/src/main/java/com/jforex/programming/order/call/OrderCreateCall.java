package com.jforex.programming.order.call;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

@FunctionalInterface
public interface OrderCreateCall {

    abstract IOrder create() throws JFException;
}
