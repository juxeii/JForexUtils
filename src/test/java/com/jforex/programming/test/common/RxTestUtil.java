package com.jforex.programming.test.common;

import java.util.concurrent.TimeUnit;

import rx.plugins.RxJavaHooks;
import rx.schedulers.TestScheduler;

public final class RxTestUtil {

    private static final RxTestUtil instance = new RxTestUtil();
    private static final TestScheduler testScheduler = new TestScheduler();

    public RxTestUtil() {
        RxJavaHooks.setOnIOScheduler(scheduler -> testScheduler);
        RxJavaHooks.setOnComputationScheduler(scheduler -> testScheduler);
        RxJavaHooks.setOnNewThreadScheduler(scheduler -> testScheduler);
    }

    public static final RxTestUtil get() {
        return instance;
    }

    public static final void advanceTimeBy(final long delayTime,
                                           final TimeUnit timeUnit) {
        testScheduler.advanceTimeBy(delayTime, timeUnit);
    }
}