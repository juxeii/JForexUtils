package com.jforex.programming.order.builder;

import java.util.function.Consumer;

public interface CommonOption<T> {

    public T onError(Consumer<Throwable> errorAction);

    public T doRetries(int noOfRetries,
                       long delayInMillis);
}
