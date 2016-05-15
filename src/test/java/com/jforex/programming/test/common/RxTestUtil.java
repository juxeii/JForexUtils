package com.jforex.programming.test.common;

import rx.plugins.RxJavaPlugins;

public final class RxTestUtil {

    private final static RxTestUtil instance = new RxTestUtil();

    static {
        RxJavaPlugins.getInstance().registerSchedulersHook(new RxSchedulerHookForTest());
        RxJavaPlugins.getInstance().registerObservableExecutionHook(new RxObservableExecutionHookForTest());
    }

    public final static RxTestUtil get() {
        return instance;
    }
}