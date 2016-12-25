package com.jforex.programming.rx;

import java.util.concurrent.TimeUnit;

public final class RetryDelay {

    private final long delay;
    private final TimeUnit timeUnit;

    public RetryDelay(final long delay,
                      final TimeUnit timeUnit) {
        this.delay = delay;
        this.timeUnit = timeUnit;
    }

    public final long delay() {
        return delay;
    }

    public final TimeUnit timeUnit() {
        return timeUnit;
    }
}
