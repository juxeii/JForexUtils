package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.test.common.CommonUtilForTest;

public class ConcurrentUtilTest extends CommonUtilForTest {

    private ConcurrentUtil concurrentUtil;

    @Mock
    private IContext contextMock;
    @Mock
    private Runnable threadMock;
    @Mock
    private Callable<IOrder> taskMock;
    @Mock
    private Future<IOrder> futureMock;

    private final static String strategyThreadPrefix = platformSettings.strategyThreadPrefix();

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        concurrentUtil = new ConcurrentUtil(contextMock);
    }

    private void setUpMocks() {
        when(contextMock.executeTask(taskMock)).thenReturn(futureMock);
    }

    @Test
    public void testExecuteOnStrategyThreadInvokesContextExecutor() {
        final Future<IOrder> future = concurrentUtil.executeOnStrategyThread(taskMock);

        verify(contextMock).executeTask(taskMock);
        assertThat(future, equalTo(futureMock));
    }

    @Test
    public void testIsStrategyThread() {
        setStrategyThread();

        assertTrue(ConcurrentUtil.isStrategyThread());
    }

    @Test
    public void testIsNotStrategyThread() {
        Thread.currentThread().setName("Not" + strategyThreadPrefix);

        assertFalse(ConcurrentUtil.isStrategyThread());
    }

    @Test
    public void testThreadNameReturnsCorrectName() {
        setStrategyThread();

        assertThat(ConcurrentUtil.threadName(), equalTo(strategyThreadPrefix));
    }
}
