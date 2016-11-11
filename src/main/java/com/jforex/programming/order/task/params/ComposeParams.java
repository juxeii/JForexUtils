package com.jforex.programming.order.task.params;

import java.util.function.Consumer;

import io.reactivex.functions.Action;

public class ComposeParams implements ComposeData {

    private Action startAction = () -> {};
    private Action completeAction = () -> {};
    private Consumer<Throwable> errorConsumer = t -> {};
    private RetryParams retryParams = new RetryParams(0, 0L);

    @Override
    public Action startAction() {
        return startAction;
    }

    @Override
    public Action completeAction() {
        return completeAction;
    }

    @Override
    public Consumer<Throwable> errorConsumer() {
        return errorConsumer;
    }

    @Override
    public RetryParams retryParams() {
        return retryParams;
    }

    public void setStartAction(final Action startAction) {
        this.startAction = startAction;
    }

    public void setCompleteAction(final Action completeAction) {
        this.completeAction = completeAction;
    }

    public void setErrorConsumer(final Consumer<Throwable> errorConsumer) {
        this.errorConsumer = errorConsumer;
    }

    public void setRetryParams(final RetryParams retryParams) {
        this.retryParams = retryParams;
    }
}
