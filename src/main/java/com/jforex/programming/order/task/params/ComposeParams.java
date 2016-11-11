package com.jforex.programming.order.task.params;

import java.util.function.Consumer;

import io.reactivex.functions.Action;

public class ComposeParams {

    private Action startAction = () -> {};
    private Action completeAction = () -> {};
    private Consumer<Throwable> errorConsumer = t -> {};
    private RetryParams retryParams = new RetryParams(0, 0L);

    public Action startAction() {
        return startAction;
    }

    public Action completeAction() {
        return completeAction;
    }

    public Consumer<Throwable> errorConsumer() {
        return errorConsumer;
    }

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
