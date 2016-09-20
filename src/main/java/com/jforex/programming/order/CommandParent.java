package com.jforex.programming.order;

public interface CommandParent<T, V> {

    public T addChild(V child);
}
