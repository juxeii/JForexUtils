package com.jforex.programming.order.call.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.misc.JFCallable;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class OrderCallExecutorTest extends CommonUtilForTest {

    private OrderCallExecutor orderCallExecutor;

    @Mock
    private JFCallable<IOrder> orderCallMock;
    @Mock
    private Future<IOrder> futureMock;
    private final TestSubscriber<IOrder> orderSubscriber = new TestSubscriber<>();
    private final IOrderForTest testOrder = IOrderForTest.buyOrderEURUSD();
    private final Runnable executorCall =
            () -> orderCallExecutor.callObservable(orderCallMock).subscribe(orderSubscriber);

    @Before
    public void setUp() throws InterruptedException, ExecutionException, JFException {
        initCommonTestFramework();
        setUpMocks();

        orderCallExecutor = new OrderCallExecutor(contextMock);
    }

    private void setUpMocks() throws InterruptedException, ExecutionException, JFException {
        when(orderCallMock.call()).thenReturn(testOrder);

        when(futureMock.get()).thenReturn(testOrder);

        when(contextMock.executeTask(orderCallMock)).thenReturn(futureMock);
    }

    private void assertWhenNotSubscribedNoExecutionHappens() {
        orderCallExecutor.callObservable(orderCallMock);

        verifyZeroInteractions(orderCallMock);
        verifyZeroInteractions(contextMock);
        verifyZeroInteractions(futureMock);
    }

    private void assertOrderEmissionAndCompletion() {
        orderSubscriber.assertCompleted();
        orderSubscriber.assertNoErrors();
        orderSubscriber.assertValueCount(1);

        assertThat(orderSubscriber.getOnNextEvents().get(0), equalTo(testOrder));
    }

    public class OnStrategyThread {

        @Before
        public void setUp() {
            CommonUtilForTest.setStrategyThread();
        }

        @Test
        public void whenNotSubscribedNoExecutionHappens() {
            assertWhenNotSubscribedNoExecutionHappens();
        }

        @Test
        public void onErrorExceptionIsEmitted() throws JFException {
            when(orderCallMock.call()).thenThrow(jfException);

            executorCall.run();

            orderSubscriber.assertError(jfException);
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                executorCall.run();
            }

            @Test
            public void orderCallIsExecuted() throws JFException {
                verify(orderCallMock).call();
            }

            @Test
            public void noConcurrentUtilCall() {
                verifyZeroInteractions(futureMock);
                verifyZeroInteractions(contextMock);
            }

            @Test
            public void testOrderIsEmittedAndCompleted() {
                assertOrderEmissionAndCompletion();
            }
        }
    }

    public class OnNotStrategyThread {

        @Before
        public void setUp() {
            CommonUtilForTest.setNotStrategyThread();
        }

        @Test
        public void whenNotSubscribedNoExecutionHappens() {
            assertWhenNotSubscribedNoExecutionHappens();
        }

        @Test
        public void onErrorExceptionIsEmitted() throws InterruptedException, ExecutionException {
            when(futureMock.get()).thenThrow(InterruptedException.class);

            executorCall.run();

            orderSubscriber.assertError(InterruptedException.class);
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                executorCall.run();
            }

            @Test
            public void executionWithConcurrentUtil() throws InterruptedException, ExecutionException {
                verify(contextMock).executeTask(orderCallMock);
                verify(futureMock).get();
            }

            @Test
            public void testOrderIsEmittedAndCompleted() {
                assertOrderEmissionAndCompletion();
            }
        }
    }
}
