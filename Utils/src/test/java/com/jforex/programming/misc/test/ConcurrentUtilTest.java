package com.jforex.programming.misc.test;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.test.common.CommonUtilForTest;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IOrder;

public class ConcurrentUtilTest extends CommonUtilForTest {

    private ConcurrentUtil concurrentUtil;

    @Mock private IContext contextMock;
    @Mock private ExecutorService executorServiceMock;
    @Mock private Runnable threadMock;
    @Mock private Callable<IOrder> taskMock;
    @Mock private Future<IOrder> futureMock;
    @Mock private ScheduledFuture<IOrder> scheduledFutureMock;

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        concurrentUtil = new ConcurrentUtil(contextMock, executorServiceMock);
    }

    private void setUpMocks() {
        doReturn(futureMock).when(executorServiceMock).submit(threadMock);
        when(executorServiceMock.submit(taskMock)).thenReturn(futureMock);
        when(contextMock.executeTask(taskMock)).thenReturn(futureMock);
    }

    private void verifyExecutorShutdown(final ExecutorService executorServiceMock) throws InterruptedException {
        verify(executorServiceMock).shutdownNow();
        verify(executorServiceMock).awaitTermination(pfs.EXECUTORSERVICE_AWAITTERMINATION_TIMEOUT(),
                                                     TimeUnit.MILLISECONDS);
    }

    @Test
    public void testExecuteForThreadStartsCorrectThread() {
        final Future<?> future = concurrentUtil.execute(threadMock);

        verify(executorServiceMock).submit(threadMock);
        assertThat(future, equalTo(futureMock));
    }

    @Test
    public void testExecuteForTaskStartsCorrectTask() {
        final Future<IOrder> future = concurrentUtil.execute(taskMock);

        verify(executorServiceMock).submit(taskMock);
        assertThat(future, equalTo(futureMock));
    }

    @Test
    public void testExecuteOnStrategyThreadInvokesContextExecutor() {
        final Future<IOrder> future = concurrentUtil.executeOnStrategyThread(taskMock);

        verify(contextMock).executeTask(taskMock);
        assertThat(future, equalTo(futureMock));
    }

    @Test
    public void testOnStopCallsShutDownNowAndAwaitTerminationOnExecutorService() throws InterruptedException {
        concurrentUtil.onStop();

        verifyExecutorShutdown(executorServiceMock);
    }

    @Test
    public void testIsStrategyThread() {
        setStrategyThread();

        assertTrue(ConcurrentUtil.isStrategyThread());
    }

    @Test
    public void testIsNotStrategyThread() {
        Thread.currentThread().setName("Not" + pfs.STRATEGY_THREAD_PREFIX());

        assertFalse(ConcurrentUtil.isStrategyThread());
    }

    @Test
    public void testThreadNameReturnsCorrectName() {
        setStrategyThread();

        assertThat(ConcurrentUtil.threadName(), equalTo(pfs.STRATEGY_THREAD_PREFIX()));
    }
}
