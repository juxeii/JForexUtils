package com.jforex.programming.test.common;

import rx.Scheduler;
import rx.plugins.RxJavaSchedulersHook;

public class RxSchedulerHookForTest extends RxJavaSchedulersHook {

    private final Scheduler scheduler;

    public RxSchedulerHookForTest(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Scheduler getIOScheduler() {
        return scheduler;
    }

    @Override
    public Scheduler getNewThreadScheduler() {
        return scheduler;
    }

    @Override
    public Scheduler getComputationScheduler() {
        return scheduler;
    }
}
