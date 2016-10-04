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
import com.dukascopy.api.JFException;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.StrategyThreadTask;
import com.jforex.programming.order.OrderTaskExecutor;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.functions.Action;

@RunWith(HierarchicalContextRunner.class)
public class OrderTaskExecutorTest extends CommonUtilForTest {

    private OrderTaskExecutor orderTaskExecutor;

    @Mock
    private StrategyThreadTask strategyThreadTask;
    @Mock
    private Callable<IOrder> orderCallableMock;
    @Captor
    private ArgumentCaptor<Callable<IOrder>> callableCaptor;
    @Captor
    private ArgumentCaptor<Action> actionCaptor;
    private final IOrder orderForTest = buyOrderEURUSD;
    private final Single<IOrder> testOrderSingle = Single.just(orderForTest);
    private Single<IOrder> returnedOrderSingle;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        when(strategyThreadTask.execute(any(Callable.class)))
            .thenReturn(testOrderSingle);

        orderTaskExecutor = new OrderTaskExecutor(strategyThreadTask, engineMock);
    }

    private void captureAndRunAction() throws Exception {
        verify(strategyThreadTask).execute(actionCaptor.capture());
        actionCaptor.getValue().run();
    }

    public class SubmitOrderSetup {

        @Before
        public void setUp() throws JFException {
            when(engineMock.submitOrder(buyParamsEURUSD.label(),
                                        buyParamsEURUSD.instrument(),
                                        buyParamsEURUSD.orderCommand(),
                                        buyParamsEURUSD.amount(),
                                        buyParamsEURUSD.price(),
                                        buyParamsEURUSD.slippage(),
                                        buyParamsEURUSD.stopLossPrice(),
                                        buyParamsEURUSD.takeProfitPrice(),
                                        buyParamsEURUSD.goodTillTime(),
                                        buyParamsEURUSD.comment()))
                                            .thenReturn(buyOrderEURUSD);

            returnedOrderSingle = orderTaskExecutor.submitOrder(buyParamsEURUSD);
        }

        @Test
        public void callableIsNotInvoked() {
            verifyZeroInteractions(orderCallableMock);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            verify(strategyThreadTask).execute(callableCaptor.capture());

            callableCaptor.getValue().call();

            verify(engineMock).submitOrder(buyParamsEURUSD.label(),
                                           buyParamsEURUSD.instrument(),
                                           buyParamsEURUSD.orderCommand(),
                                           buyParamsEURUSD.amount(),
                                           buyParamsEURUSD.price(),
                                           buyParamsEURUSD.slippage(),
                                           buyParamsEURUSD.stopLossPrice(),
                                           buyParamsEURUSD.takeProfitPrice(),
                                           buyParamsEURUSD.goodTillTime(),
                                           buyParamsEURUSD.comment());
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
            when(engineMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                .thenReturn(orderForTest);

            returnedOrderSingle = orderTaskExecutor.mergeOrders(mergeOrderLabel, toMergeOrders);
        }

        @Test
        public void callableIsNotInvoked() {
            verifyZeroInteractions(orderCallableMock);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            verify(strategyThreadTask).execute(callableCaptor.capture());

            callableCaptor.getValue().call();

            verify(engineMock).mergeOrders(mergeOrderLabel, toMergeOrders);
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
