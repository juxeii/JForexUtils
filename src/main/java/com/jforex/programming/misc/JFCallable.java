package com.jforex.programming.misc;

import java.util.concurrent.Callable;

import com.dukascopy.api.JFException;

@FunctionalInterface
public interface JFCallable<V> extends Callable<V> {

    public V call() throws JFException;
}
