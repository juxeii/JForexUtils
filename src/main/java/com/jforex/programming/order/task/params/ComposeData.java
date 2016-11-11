package com.jforex.programming.order.task.params;

import java.util.function.Consumer;

import io.reactivex.functions.Action;

public interface ComposeData {

    public Action startAction();

    public Action completeAction();

    public Consumer<Throwable> errorConsumer();

    public RetryParams retryParams();
}
