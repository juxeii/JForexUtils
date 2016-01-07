package com.jforex.programming.order.call;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

@FunctionalInterface
public interface OrderCall {

    abstract IOrder run() throws JFException;
}
