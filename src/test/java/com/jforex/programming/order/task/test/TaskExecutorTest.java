package com.jforex.programming.order.task.test;

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
import com.dukascopy.api.OfferSide;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.StrategyThreadRunner;
import com.jforex.programming.order.task.TaskExecutor;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.functions.Action;

@RunWith(HierarchicalContextRunner.class)
public class TaskExecutorTest extends CommonUtilForTest {

    private TaskExecutor taskExecutor;

    @Mock
    private StrategyThreadRunner strategyThreadTask;
    @Mock
    private Callable<IOrder> orderCallableMock;
    @Captor
    private ArgumentCaptor<Callable<IOrder>> callableCaptor;
    @Captor
    private ArgumentCaptor<Action> actionCaptor;
    private final IOrder orderForTest = buyOrderEURUSD;
    private final Single<IOrder> testOrderSingle = Single.just(orderForTest);
    private Single<IOrder> returnedOrderSingle;
    private final double closeAmount = 0.12;
    private final double closePrice = 1.1234;
    private final double closeSlippage = 5.5;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        when(strategyThreadTask.execute(any(Callable.class)))
            .thenReturn(testOrderSingle);

        taskExecutor = new TaskExecutor(strategyThreadTask, engineMock);
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

            returnedOrderSingle = taskExecutor.submitOrder(buyParamsEURUSD);
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

            returnedOrderSingle = taskExecutor.mergeOrders(mergeOrderLabel, toMergeOrders);
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

    public class CloseWithAmountSetup {

        @Before
        public void setUp() {
            taskExecutor.close(orderForTest, closeAmount);
        }

        @Test
        public void closeIsNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            captureAndRunAction();
            verify(orderForTest).close(closeAmount);
        }
    }

    public class CloseWithAmountAndPriceAndSlippageSetup {

        @Before
        public void setUp() {
            taskExecutor.close(orderForTest,
                               closeAmount,
                               closePrice,
                               closeSlippage);
        }

        @Test
        public void closeIsNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            captureAndRunAction();
            verify(orderForTest).close(closeAmount,
                                       closePrice,
                                       closeSlippage);
        }
    }

    public class SetLabelSetup {

        private final String newLabel = "newLabel";

        @Before
        public void setUp() {
            taskExecutor.setLabel(orderForTest, newLabel);
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
            taskExecutor.setGoodTillTime(orderForTest, newGTT);
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
            taskExecutor.setRequestedAmount(orderForTest, newRequestedAmount);
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
            taskExecutor.setOpenPrice(orderForTest, newOpenPrice);
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

    public class SetStopLossWithOfferSideAndTrailingStepSetup {

        private final double newSL = 1.1234;
        private final double trailingStep = 11.3;

        @Before
        public void setUp() {
            taskExecutor.setStopLossPrice(orderForTest,
                                          newSL,
                                          OfferSide.ASK,
                                          trailingStep);
        }

        @Test
        public void setStopLossNotCalled() {
            verifyZeroInteractions(orderForTest);
        }

        @Test
        public void taskExecutorCallsOnStrategyThreadWithAction() throws Exception {
            captureAndRunAction();
            verify(orderForTest).setStopLossPrice(newSL,
                                                  OfferSide.ASK,
                                                  trailingStep);
        }
    }

    public class SetTakeProfitSetup {

        private final double newTP = 1.1234;

        @Before
        public void setUp() {
            taskExecutor.setTakeProfitPrice(orderForTest, newTP);
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
