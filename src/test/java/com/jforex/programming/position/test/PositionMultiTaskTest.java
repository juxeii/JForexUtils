package com.jforex.programming.position.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionMultiTask;
import com.jforex.programming.position.PositionSingleTask;
import com.jforex.programming.position.RestoreSLTPData;
import com.jforex.programming.test.common.PositionCommonTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionMultiTaskTest extends PositionCommonTest {

    private PositionMultiTask positionMultiTask;

    @Mock
    private PositionSingleTask positionSingleTaskMock;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();

    @Before
    public void setUp() {
        initCommonTestFramework();

        positionMultiTask = new PositionMultiTask(positionSingleTaskMock);
    }

    public class RestoreSLTPSetup {

        private final double restoreSL = 1.10234;
        private final double restoreTP = 1.11234;
        private final RestoreSLTPData restoreSLTPData = new RestoreSLTPData(restoreSL, restoreTP);
        private final Runnable restoreSLTPCompletableCall =
                () -> positionMultiTask
                        .restoreSLTPObservable(orderUnderTest, restoreSLTPData)
                        .subscribe(taskSubscriber);

        private void setSLMockResult(final Observable<OrderEvent> observable) {
            when(positionSingleTaskMock.setSLObservable(orderUnderTest, restoreSL))
                    .thenReturn(observable);
        }

        private void setTPMockResult(final Observable<OrderEvent> observable) {
            when(positionSingleTaskMock.setTPObservable(orderUnderTest, restoreTP))
                    .thenReturn(observable);
        }

        @Before
        public void setUp() {
            orderUnderTest.setState(IOrder.State.FILLED);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            setSLMockResult(busyObservable());

            restoreSLTPCompletableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class RestoreSLOK {

            @Before
            public void setUp() {
                setSLMockResult(doneObservable());
            }

            public class RestoreTPOK {

                @Before
                public void setUp() {
                    setTPMockResult(doneObservable());

                    restoreSLTPCompletableCall.run();
                }

                @Test
                public void testRestoreSLTPOnSingleTaskHasBeenCalled() {
                    verify(positionSingleTaskMock).setSLObservable(orderUnderTest, restoreSL);
                    verify(positionSingleTaskMock).setTPObservable(orderUnderTest, restoreTP);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }
        }

        public class RestoreSLWithJFException {

            @Before
            public void setUp() {
                setSLMockResult(exceptionObservable());

                restoreSLTPCompletableCall.run();
            }

            @Test
            public void testSetSLOnOrderHasBeenCalledWithoutRetry() {
                verify(positionSingleTaskMock).setSLObservable(orderUnderTest, restoreSL);
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }
    }

    public class RemoveTPSLCompletableSetup {

        private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
        private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
        private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();
        private final Set<IOrder> filledOrders = Sets.newHashSet(buyOrder, sellOrder);

        private final Runnable removeTPSLCompletableCall =
                () -> positionMultiTask.removeTPSLObservable(filledOrders).subscribe(taskSubscriber);

        private void setSLTaskMockResult(final IOrder order,
                                         final double newSL,
                                         final Observable<OrderEvent> observable) {
            when(positionSingleTaskMock.setSLObservable(order, newSL))
                    .thenReturn(observable);
        }

        private void setTPTaskMockResult(final IOrder order,
                                         final double newTP,
                                         final Observable<OrderEvent> observable) {
            when(positionSingleTaskMock.setTPObservable(order, newTP))
                    .thenReturn(observable);
        }

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
            sellOrder.setState(IOrder.State.FILLED);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            setTPTaskMockResult(buyOrder, noTP, busyObservable());
            setSLTaskMockResult(buyOrder, noSL, busyObservable());

            removeTPSLCompletableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class RemoveTPSLOnBuyOK {

            @Before
            public void setUp() {
                setTPTaskMockResult(buyOrder, noTP, doneObservable());
                setSLTaskMockResult(buyOrder, noSL, doneObservable());
            }

            public class RemoveTPSLOnSellOK {

                @Before
                public void setUp() {
                    setTPTaskMockResult(sellOrder, noTP, doneObservable());
                    setSLTaskMockResult(sellOrder, noSL, doneObservable());

                    removeTPSLCompletableCall.run();
                }

                @Test
                public void testRemoveTPSLOnSingleTaskForAllOrdersHasBeenCalled() {
                    verify(positionSingleTaskMock).setTPObservable(buyOrder, noTP);
                    verify(positionSingleTaskMock).setSLObservable(buyOrder, noSL);
                    verify(positionSingleTaskMock).setTPObservable(sellOrder, noTP);
                    verify(positionSingleTaskMock).setSLObservable(sellOrder, noSL);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }
        }

        public class SingleTaskCallWithJFException {

            @Before
            public void setUp() {
                setTPTaskMockResult(buyOrder, noTP, exceptionObservable());
                setTPTaskMockResult(sellOrder, noTP, exceptionObservable());

                removeTPSLCompletableCall.run();
            }

            @Test
            public void testSetTPOnOrderHasBeenCalledWithoutRetry() {
                verify(positionSingleTaskMock).setTPObservable(any(), eq(noTP));
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }
    }
}
