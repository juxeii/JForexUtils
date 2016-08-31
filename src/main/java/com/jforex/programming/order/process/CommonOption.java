package com.jforex.programming.order.process;

import java.util.function.Consumer;

public interface CommonOption {

    public CommonBuilder onError(Consumer<Throwable> errorAction);

    public CommonBuilder doRetries(int noOfRetries,
                                   long delayInMillis);

    public <T> T build();
}
