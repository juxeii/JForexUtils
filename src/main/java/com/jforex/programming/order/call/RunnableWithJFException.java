package com.jforex.programming.order.call;

import com.dukascopy.api.JFException;

@FunctionalInterface
public interface RunnableWithJFException {

    public void run() throws JFException;
}
