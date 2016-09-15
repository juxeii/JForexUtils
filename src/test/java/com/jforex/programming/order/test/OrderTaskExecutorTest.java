package com.jforex.programming.order.test;

import static com.jforex.programming.order.OrderStaticUtil.runnableToCallable;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.misc.TaskExecutor;
import com.jforex.programming.order.OrderTaskExecutor;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;

@RunWith(HierarchicalContextRunner.class)
public class OrderTaskExecutorTest extends CommonUtilForTest {

    private OrderTaskExecutor orderTaskExecutor;

    @Mock
    private TaskExecutor taskExecutorMock;
    @Mock
    private IEngineUtil engineUtilMock;
    @Mock
    private Callable<IOrder> orderCallableMock;
    @Captor
    private ArgumentCaptor<Callable<Void>> callableCaptor;
    private final IOrder orderForTest = buyOrderEURUSD;
    private final Single<IOrder> testOrderSingle = Single.just(orderForTest);
    private Single<IOrder> returnedOrderSingle;

    @Before
    public void setUp() throws Exception {
        setUpMocks();

        orderTaskExecutor = new OrderTaskExecutor(taskExecutorMock, engineUtilMock);
    }

    private void setUpMocks() throws Exception {
    }

    private void setUpTaskExecutorSingle(final Callable<IOrder> callable,
                                         final Single<IOrder> single) {
        when(taskExecutorMock.onStrategyThread(callable))
            .thenReturn(single);
    }

    private void setUpTaskExecutorCompletableSingle(final Callable<Void> callable) {
        when(taskExecutorMock.onStrategyThread(callableCaptor.capture()))
            .thenReturn(Single.fromCallable(callable));
    }

    public class SubmitOrderSetup {

        @Before
        public void setUp() {
            when(engineUtilMock.submitCallable(buyParamsEURUSD)).thenReturn(orderCallableMock);

            setUpTaskExecutorSingle(orderCallableMock, testOrderSingle);

            returnedOrderSingle = orderTaskExecutor.submitOrder(buyParamsEURUSD);
        }

        @Test
        public void callableIsNotInvoked() {
            verifyZeroInteractions(orderCallableMock);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithCallable() {
            verify(taskExecutorMock).onStrategyThread(orderCallableMock);
        }

        @Test
        public void callReturnsSingleInstanceFromTaskExecutor() {
            assertThat(returnedOrderSingle, equalTo(testOrderSingle));
        }

        @Test
        public void callReturnsCorrectOrderInstance() {
            final IOrder returnedOrder = returnedOrderSingle.blockingGet();

            assertThat(returnedOrder, equalTo(orderForTest));
        }
    }

    public class MergeOrdersSetup {

        private final String mergeOrderLabel = "mergeOrderLabel";
        private final Collection<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

        @Before
        public void setUp() throws Exception {
            when(engineUtilMock.mergeCallable(mergeOrderLabel, toMergeOrders))
                .thenReturn(orderCallableMock);

            setUpTaskExecutorSingle(orderCallableMock, testOrderSingle);

            returnedOrderSingle = orderTaskExecutor.mergeOrders(mergeOrderLabel, toMergeOrders);
        }

        @Test
        public void callableIsNotInvoked() {
            verifyZeroInteractions(orderCallableMock);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithCallable() {
            verify(taskExecutorMock).onStrategyThread(orderCallableMock);
        }

        @Test
        public void callReturnsSingleInstanceFromTaskExecutor() {
            assertThat(returnedOrderSingle, equalTo(testOrderSingle));
        }

        @Test
        public void callReturnsCorrectOrderInstance() {
            final IOrder returnedOrder = returnedOrderSingle.blockingGet();

            assertThat(returnedOrder, equalTo(orderForTest));
        }
    }

    public class CloseSetup {

        private final Callable<Void> closeCallable = runnableToCallable(() -> orderForTest.close());

        @Before
        public void setUp() {
            setUpTaskExecutorCompletableSingle(closeCallable);

            orderTaskExecutor.close(orderForTest);
        }

        @Test
        public void closeIsNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithCallable() throws Exception {
            callableCaptor.getValue().call();

            verify(orderForTest).close();
            verify(taskExecutorMock).onStrategyThread(any());
        }
    }
}
