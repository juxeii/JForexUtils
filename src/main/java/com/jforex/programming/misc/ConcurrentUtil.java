package com.jforex.programming.misc;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;

import com.jforex.programming.settings.PlatformSettings;

import com.dukascopy.api.IContext;

public class ConcurrentUtil {

    private final IContext context;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);

    public ConcurrentUtil(final IContext context) {
        this.context = context;
    }

    public <T> Future<T> executeOnStrategyThread(final Callable<T> task) {
        return context.executeTask(task);
    }

    public static boolean isStrategyThread() {
        return StringUtils.startsWith(threadName(), platformSettings.strategyThreadPrefix());
    }

    public static String threadName() {
        return Thread.currentThread().getName();
    }
}
