package com.jforex.programming.misc;

import com.dukascopy.api.JFException;

@FunctionalInterface
public interface JFRunnable {

    public void run() throws JFException, Exception;
}
