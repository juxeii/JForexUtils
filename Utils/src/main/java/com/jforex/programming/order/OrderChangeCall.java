package com.jforex.programming.order;

import com.dukascopy.api.JFException;

@FunctionalInterface
public interface OrderChangeCall {

    abstract void run() throws JFException;
}
