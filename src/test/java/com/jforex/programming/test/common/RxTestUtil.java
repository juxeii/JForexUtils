package com.jforex.programming.test.common;

import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.plugins.RxJavaPlugins;
import rx.plugins.RxJavaSchedulersHook;
import rx.schedulers.TestScheduler;

public final class RxTestUtil {

    private final static RxTestUtil instance = new RxTestUtil();
    private final static TestScheduler testScheduler = new TestScheduler();

    static {
        try {
            RxJavaPlugins.getInstance().registerSchedulersHook(new RxJavaSchedulersHook() {
                @Override
                public Scheduler getIOScheduler() {
                    return testScheduler;
                }

                @Override
                public Scheduler getComputationScheduler() {
                    return testScheduler;
                }

                @Override
                public Scheduler getNewThreadScheduler() {
                    return testScheduler;
                }
            });
        } catch (final IllegalStateException e) {
            throw new IllegalStateException("Schedulers class already initialized. " +
                    "Ensure you always use the TestSchedulerProxy in unit tests.");
        }
    }

    public final static RxTestUtil get() {
        return instance;
    }

    public final void advanceTimeBy(final long delayTime,
                                    final TimeUnit unit) {
        testScheduler.advanceTimeBy(delayTime, unit);
    }
}