package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.jforex.programming.order.process.CommonBuilder;

public interface CommonOption {

    public CommonBuilder onError(Consumer<Throwable> errorAction);

    public CommonBuilder doRetries(int noOfRetries,
                                   long delayInMillis);

    public <V> V build();
}
