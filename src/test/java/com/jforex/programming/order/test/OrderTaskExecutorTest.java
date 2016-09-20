package com.jforex.programming.order.test;

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
import io.reactivex.functions.Action;

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
    private ArgumentCaptor<Action> actionCaptor;
    private final IOrder orderForTest = buyOrderEURUSD;
    private final Single<IOrder> testOrderSingle = Single.just(orderForTest);
    private Single<IOrder> returnedOrderSingle;

    @Before
    public void setUp() throws Exception {
        orderTaskExecutor = new OrderTaskExecutor(taskExecutorMock, engineUtilMock);
    }

    private void setUpTaskExecutorSingle(final Callable<IOrder> callable,
                                         final Single<IOrder> single) {
        when(taskExecutorMock.onStrategyThread(callable))
            .thenReturn(single);
    }

    private void captureAndRunAction() throws Exception {
        verify(taskExecutorMock).onStrategyThread(actionCaptor.capture());
        actionCaptor.getValue().run();
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
        public void taskExecutorCallsOnStrategyThreadWithAction() {
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
        public void taskExecutorCallsOnStrategyThreadWithAction() {
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

        @Before
        public void setUp() {
            orderTaskExecutor.close(orderForTest);
        }

        @Test
        public void closeIsNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            captureAndRunAction();
            verify(orderForTest).close();
        }
    }

    public class SetLabelSetup {

        private final String newLabel = "newLabel";

        @Before
        public void setUp() {
            orderTaskExecutor.setLabel(orderForTest, newLabel);
        }

        @Test
        public void setLabelIsNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            captureAndRunAction();
            verify(orderForTest).setLabel(newLabel);
        }
    }

    public class SetGTTSetup {

        private final long newGTT = 1L;

        @Before
        public void setUp() {
            orderTaskExecutor.setGoodTillTime(orderForTest, newGTT);
        }

        @Test
        public void setGoodTillTimeIsNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            captureAndRunAction();
            verify(orderForTest).setGoodTillTime(newGTT);
        }
    }

    public class SetRequestedAmountSetup {

        private final double newRequestedAmount = 0.12;

        @Before
        public void setUp() {
            orderTaskExecutor.setRequestedAmount(orderForTest, newRequestedAmount);
        }

        @Test
        public void setRequestedAmountNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            captureAndRunAction();
            verify(orderForTest).setRequestedAmount(newRequestedAmount);
        }
    }

    public class SetOpenPriceSetup {

        private final double newOpenPrice = 1.1234;

        @Before
        public void setUp() {
            orderTaskExecutor.setOpenPrice(orderForTest, newOpenPrice);
        }

        @Test
        public void setOpenPriceNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            captureAndRunAction();
            verify(orderForTest).setOpenPrice(newOpenPrice);
        }
    }

    public class SetStopLossSetup {

        private final double newSL = 1.1234;

        @Before
        public void setUp() {
            orderTaskExecutor.setStopLossPrice(orderForTest, newSL);
        }

        @Test
        public void setStopLossNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            captureAndRunAction();
            verify(orderForTest).setStopLossPrice(newSL);
        }
    }

    public class SetTakeProfitSetup {

        private final double newTP = 1.1234;

        @Before
        public void setUp() {
            orderTaskExecutor.setTakeProfitPrice(orderForTest, newTP);
        }

        @Test
        public void setTakeProfitNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            captureAndRunAction();
            verify(orderForTest).setTakeProfitPrice(newTP);
        }
    }
}
