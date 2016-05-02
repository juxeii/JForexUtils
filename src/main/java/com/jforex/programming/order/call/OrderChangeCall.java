package com.jforex.programming.order.call;

import com.dukascopy.api.JFException;

@FunctionalInterface
public interface OrderChangeCall {

    public void change() throws JFException;
}
