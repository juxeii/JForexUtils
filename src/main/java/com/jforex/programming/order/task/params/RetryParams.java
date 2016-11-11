package com.jforex.programming.order.task.params;

public class RetryParams {

    private final int noOfRetries;
    private final long delayInMillis;

    public RetryParams(final int noOfRetries,
                       final long delayInMillis) {
        this.noOfRetries = noOfRetries;
        this.delayInMillis = delayInMillis;
    }

    public int noOfRetries() {
        return noOfRetries;
    }

    public long delayInMillis() {
        return delayInMillis;
    }
}
