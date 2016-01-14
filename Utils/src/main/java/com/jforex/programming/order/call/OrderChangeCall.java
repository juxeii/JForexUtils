package com.jforex.programming.order.call;

import com.dukascopy.api.JFException;

@FunctionalInterface
public interface OrderChangeCall {

    abstract void run() throws JFException;
}
