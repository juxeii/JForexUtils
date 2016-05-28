package com.jforex.programming.misc;

import com.dukascopy.api.JFException;

@FunctionalInterface
public interface RunnableWithJFException {

    public void run() throws JFException;
}
