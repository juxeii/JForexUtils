package com.jforex.programming.test.common;

import rx.Scheduler;
import rx.plugins.RxJavaSchedulersHook;
import rx.schedulers.Schedulers;

public class RxSchedulerHookForTest extends RxJavaSchedulersHook {

    @Override
    public Scheduler getIOScheduler() {
        return Schedulers.immediate();
    }

    @Override
    public Scheduler getNewThreadScheduler() {
        return Schedulers.immediate();
    }

    @Override
    public Scheduler getComputationScheduler() {
        return Schedulers.immediate();
    }
}
