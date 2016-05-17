package com.jforex.programming.misc;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IContext;
import com.jforex.programming.settings.PlatformSettings;

public class ConcurrentUtil {

    private final IContext context;
    private final ExecutorService executorService;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private final static Logger logger = LogManager.getLogger(ConcurrentUtil.class);

    public ConcurrentUtil(final IContext context,
                          final ExecutorService executorService) {
        this.context = context;
        this.executorService = executorService;
    }

    public Future<?> execute(final Runnable task) {
        return executorService.submit(task);
    }

    public <T> Future<T> execute(final Callable<T> task) {
        return executorService.submit(task);
    }

    public <T> Future<T> executeOnStrategyThread(final Callable<T> task) {
        return context.executeTask(task);
    }

    public void onStop() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(platformSettings.terminationTimeoutExecutorService(), MILLISECONDS);
        } catch (final InterruptedException e) {
            logger.error("Exception occured while shutdown executor! Message: " + e.getMessage());
        }
    }

    public static boolean isStrategyThread() {
        return StringUtils.startsWith(threadName(), platformSettings.strategyThreadPrefix());
    }

    public static String threadName() {
        return Thread.currentThread().getName();
    }
}
