package com.jforex.programming.test.common;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;

import com.jforex.programming.settings.PlatformSettings;

import rx.Scheduler;
import rx.plugins.RxJavaPlugins;
import rx.plugins.RxJavaSchedulersHook;
import rx.schedulers.TestScheduler;

public final class RxTestUtil {

    private static final RxTestUtil instance = new RxTestUtil();
    private static final TestScheduler testScheduler = new TestScheduler();

    private static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private static final long timeDelayForAllRetries =
            platformSettings.maxRetriesOnOrderFail() * platformSettings.delayOnOrderFailRetry();

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

    public static final RxTestUtil get() {
        return instance;
    }

    public final void advanceTimeBy(final long delayTime,
                                    final TimeUnit unit) {
        testScheduler.advanceTimeBy(delayTime, unit);
    }

    public final void advanceTimeForAllOrderRetries() {
        advanceTimeBy(timeDelayForAllRetries, TimeUnit.MILLISECONDS);
    }
}