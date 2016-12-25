package com.jforex.programming.order.task.params;

import com.jforex.programming.rx.RetryDelayFunction;

public class RetryParams {

    private final int noOfRetries;
    private final RetryDelayFunction delayFunction;

    public RetryParams(final int noOfRetries,
                       final RetryDelayFunction delayFunction) {
        this.noOfRetries = noOfRetries;
        this.delayFunction = delayFunction;
    }

    public int noOfRetries() {
        return noOfRetries;
    }

    public RetryDelayFunction delayFunction() {
        return delayFunction;
    }
}
