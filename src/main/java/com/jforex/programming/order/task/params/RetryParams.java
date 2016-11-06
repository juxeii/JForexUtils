package com.jforex.programming.order.task.params;

public interface RetryParams {

    public int noOfRetries();

    public long delayInMillis();
}
