package com.jforex.programming.test.common;

import java.util.concurrent.TimeUnit;

import rx.plugins.RxJavaPlugins;
import rx.schedulers.TestScheduler;

public final class RxTestUtil {

    private final static RxTestUtil instance = new RxTestUtil();
    private final static TestScheduler testScheduler = new TestScheduler();

    static {
        RxJavaPlugins.getInstance().registerSchedulersHook(new RxSchedulerHookForTest(testScheduler));
        // RxJavaPlugins.getInstance().registerObservableExecutionHook(new
        // RxObservableExecutionHookForTest());
    }

    public final static RxTestUtil get() {
        return instance;
    }

    public final void advanceTimeBy(final long delayTime,
                                    final TimeUnit unit) {
        testScheduler.advanceTimeBy(delayTime, unit);
    }
}