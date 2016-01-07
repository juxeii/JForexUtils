package com.jforex.programming.order.call.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.call.OrderCall;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderExecutorResult;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class OrderCallExecutorTest extends CommonUtilForTest {

    private OrderCallExecutor orderCallExecutor;

    @Mock private OrderCall orderCallMock;
    @Mock private Future<IOrder> futureMock;
    private final IOrderForTest order = IOrderForTest.buyOrderEURUSD();
    private OrderExecutorResult orderExecutorResult;
    private final Exception testException = new InterruptedException();

    @Before
    public void setUp() throws InterruptedException, ExecutionException {
        initCommonTestFramework();
        setUpMocks();

        orderCallExecutor = new OrderCallExecutor(concurrentUtilMock);
    }

    @SuppressWarnings("unchecked")
    private void setUpMocks() throws InterruptedException, ExecutionException {
        when(futureMock.get()).thenReturn(order);

        doAnswer(invocation -> {
            final Callable<IOrder> orderCallable = (Callable<IOrder>) (invocation.getArguments())[0];
            orderCallable.call();
            return futureMock;
        }).when(concurrentUtilMock).executeOnStrategyThread(any());
    }

    @Test
    public void testWhenOnStrategyThreadNoConcurrentUtilCall() throws JFException {
        CommonUtilForTest.setStrategyThread();

        orderCallExecutor.run(orderCallMock);

        verify(orderCallMock).run();
        verifyZeroInteractions(concurrentUtilMock);
    }

    @Test
    public void testWhenOnNonStrategyThreadConcurrentUtilExecutesOrderRunnable() throws JFException {
        CommonUtilForTest.setNotStrategyThread();

        orderCallExecutor.run(orderCallMock);

        verify(orderCallMock).run();
        verify(concurrentUtilMock).executeOnStrategyThread((any()));
    }

    @Test
    public void testVerifyExecutorResultContents() {
        orderExecutorResult = orderCallExecutor.run(orderCallMock);

        assertThat(orderExecutorResult.orderOpt().get(), equalTo(order));
        assertFalse(orderExecutorResult.exceptionOpt().isPresent());
    }

    @Test
    public void testFutureThrowsExecutorResultContentsAreCorrect() throws InterruptedException,
                                                                   ExecutionException {
        when(futureMock.get()).thenThrow(testException);

        orderExecutorResult = orderCallExecutor.run(orderCallMock);

        assertFalse(orderExecutorResult.orderOpt().isPresent());
        assertThat(orderExecutorResult.exceptionOpt().get(), equalTo(testException));
    }

    @Test
    public void testWhenOrderCallThrowsExecutorResultContentsAreCorrect() throws InterruptedException,
                                                                          ExecutionException, JFException {
        when(orderCallMock.run()).thenThrow(jfException);

        orderExecutorResult = orderCallExecutor.run(orderCallMock);

        assertFalse(orderExecutorResult.orderOpt().isPresent());
        assertThat(orderExecutorResult.exceptionOpt().get(), equalTo(jfException));
    }
}
