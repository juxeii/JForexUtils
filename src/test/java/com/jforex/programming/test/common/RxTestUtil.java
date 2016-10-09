package com.jforex.programming.test.common;

import java.util.concurrent.TimeUnit;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.TestScheduler;

public final class RxTestUtil {

    private static final RxTestUtil instance = new RxTestUtil();
    private static final TestScheduler testScheduler = new TestScheduler();

    public RxTestUtil() {
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> testScheduler);
    }

    public static final RxTestUtil get() {
        return instance;
    }

    public static final void advanceTimeBy(final long delayTime,
                                           final TimeUnit timeUnit) {
        testScheduler.advanceTimeBy(delayTime, timeUnit);
    }

    public static final void advanceTimeInMillisBy(final long delayTime) {
        advanceTimeBy(delayTime, TimeUnit.MILLISECONDS);
    }
}
