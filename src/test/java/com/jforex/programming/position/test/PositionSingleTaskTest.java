package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.PositionSingleTask;
import com.jforex.programming.test.common.PositionCommonTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionSingleTaskTest extends PositionCommonTest {

    private PositionSingleTask positionSingleTask;

    @Mock
    private OrderCreateUtil orderCreateUtilMock;
    @Mock
    private OrderChangeUtil orderChangeUtilMock;
    @Captor
    private ArgumentCaptor<Supplier<Observable<OrderEvent>>> orderCallCaptor;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();

    private void assertOrderEventNotification(final OrderEvent expectedEvent) {
        taskSubscriber.assertValueCount(1);

        final OrderEvent orderEvent = taskSubscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(expectedEvent.order()));
        assertThat(orderEvent.type(), equalTo(expectedEvent.type()));
    }

    @Before
    public void setUp() {
        initCommonTestFramework();

        positionSingleTask = new PositionSingleTask(orderCreateUtilMock, orderChangeUtilMock);
    }

    public class SetSLSetup {

        @Before
        public void setUp() {
            orderUnderTest.setState(IOrder.State.FILLED);
        }

        public class SLIsAlreadySet {

            private final double orderSL = orderUnderTest.getStopLossPrice();

            @Before
            public void setUp() {
                positionSingleTask.setSLObservable(orderUnderTest, orderSL)
                        .subscribe(taskSubscriber);
            }

            @Test
            public void testNoCallToChangeUtil() {
                verify(orderChangeUtilMock, never()).setStopLossPrice(orderUnderTest, orderSL);
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class SLIsNew {

            private final OrderEvent changedSLEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.SL_CHANGE_OK);
            private final OrderEvent rejectEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.CHANGE_SL_REJECTED);
            private final static double newSL = 1.10123;
            private final Runnable setSLObservableCall =
                    () -> positionSingleTask.setSLObservable(orderUnderTest, newSL).subscribe(taskSubscriber);

            private void setSLChangeMockResult(final Observable<OrderEvent> observable) {
                when(orderChangeUtilMock.setStopLossPrice(orderUnderTest, newSL))
                        .thenReturn(observable);
            }

            @Test
            public void testSubscriberNotYetCompletedWhenChangeUtilIsBusy() {
                setSLChangeMockResult(busyObservable());

                setSLObservableCall.run();

                taskSubscriber.assertNotCompleted();
            }

            public class SetSLWithJFException {

                @Before
                public void setUp() {
                    setSLChangeMockResult(exceptionObservable());

                    setSLObservableCall.run();
                }

                @Test
                public void testSetSLOnChangeUtilHasBeenCalledWithoutRetry() {
                    verify(orderChangeUtilMock).setStopLossPrice(orderUnderTest, newSL);
                }

                @Test
                public void testSubscriberGetsJFExceptionNotification() {
                    assertJFException(taskSubscriber);
                }
            }

            public class SetSLCallWhichExceedsRetries {

                @Before
                public void setUp() {
                    setRetryExceededMock(() -> orderChangeUtilMock.setStopLossPrice(orderUnderTest, newSL),
                                         rejectEvent);

                    setSLObservableCall.run();

                    rxTestUtil.advanceTimeForAllOrderRetries();
                }

                @Test
                public void testSetSLCalledWithAllRetries() {
                    verify(orderChangeUtilMock, times(retryExceedCount))
                            .setStopLossPrice(orderUnderTest, newSL);
                }

                @Test
                public void testSubscriberGetsRejectExceptionNotification() {
                    assertRejectException(taskSubscriber);
                }
            }

            public class SetSLCallWithFullRetriesThenSuccess {

                @Before
                public void setUp() {
                    setFullRetryMock(() -> orderChangeUtilMock.setStopLossPrice(orderUnderTest, newSL),
                                     rejectEvent);

                    setSLObservableCall.run();

                    rxTestUtil.advanceTimeForAllOrderRetries();
                }

                @Test
                public void testSetSLCalledWithAllRetries() {
                    verify(orderChangeUtilMock, times(retryExceedCount))
                            .setStopLossPrice(orderUnderTest, newSL);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }

            public class SetSLOKCall {

                @Before
                public void setUp() {
                    setSLChangeMockResult(doneEventObservable(changedSLEvent));

                    setSLObservableCall.run();
                }

                @Test
                public void testSetSLOnChangeUtilHasBeenCalledCorrect() {
                    verify(orderChangeUtilMock).setStopLossPrice(orderUnderTest, newSL);
                }

                @Test
                public void testSubscriberHasBeenNotifiedWithOrderEvent() {
                    assertOrderEventNotification(changedSLEvent);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }
        }
    }

    public class SetTPSetup {

        @Before
        public void setUp() {
            orderUnderTest.setState(IOrder.State.FILLED);
        }

        public class TPIsAlreadySet {

            private final double orderTP = orderUnderTest.getTakeProfitPrice();

            @Before
            public void setUp() {
                positionSingleTask.setTPObservable(orderUnderTest, orderTP)
                        .subscribe(taskSubscriber);
            }

            @Test
            public void testNoCallToChangeUtil() {
                verify(orderChangeUtilMock, never()).setTakeProfitPrice(orderUnderTest, orderTP);
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class TPIsNew {

            private final OrderEvent changedTPEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.TP_CHANGE_OK);
            private final OrderEvent rejectEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.CHANGE_TP_REJECTED);
            private final static double newTP = 1.12123;
            private final Runnable setTPObservableCall =
                    () -> positionSingleTask.setTPObservable(orderUnderTest, newTP).subscribe(taskSubscriber);

            private void setTPChangeMockResult(final Observable<OrderEvent> observable) {
                when(orderChangeUtilMock.setTakeProfitPrice(orderUnderTest, newTP))
                        .thenReturn(observable);
            }

            @Test
            public void testSubscriberNotYetCompletedWhenChangeUtilIsBusy() {
                setTPChangeMockResult(busyObservable());

                setTPObservableCall.run();

                taskSubscriber.assertNotCompleted();
            }

            public class SetTPWithJFException {

                @Before
                public void setUp() {
                    setTPChangeMockResult(exceptionObservable());

                    setTPObservableCall.run();
                }

                @Test
                public void testSetTPOnChangeUtilHasBeenCalledWithoutRetry() {
                    verify(orderChangeUtilMock).setTakeProfitPrice(orderUnderTest, newTP);
                }

                @Test
                public void testSubscriberGetsJFExceptionNotification() {
                    assertJFException(taskSubscriber);
                }
            }

            public class SetTPCallWhichExceedsRetries {

                @Before
                public void setUp() {
                    setRetryExceededMock(() -> orderChangeUtilMock.setTakeProfitPrice(orderUnderTest, newTP),
                                         rejectEvent);

                    setTPObservableCall.run();

                    rxTestUtil.advanceTimeForAllOrderRetries();
                }

                @Test
                public void testSetSLCalledWithAllRetries() {
                    verify(orderChangeUtilMock, times(retryExceedCount))
                            .setTakeProfitPrice(orderUnderTest, newTP);
                }

                @Test
                public void testSubscriberGetsRejectExceptionNotification() {
                    assertRejectException(taskSubscriber);
                }
            }

            public class SetTPCallWithFullRetriesThenSuccess {

                @Before
                public void setUp() {
                    setFullRetryMock(() -> orderChangeUtilMock.setTakeProfitPrice(orderUnderTest, newTP),
                                     rejectEvent);

                    setTPObservableCall.run();

                    rxTestUtil.advanceTimeForAllOrderRetries();
                }

                @Test
                public void testSetTPCalledWithAllRetries() {
                    verify(orderChangeUtilMock, times(retryExceedCount))
                            .setTakeProfitPrice(orderUnderTest, newTP);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }

            public class SetTPOKCall {

                @Before
                public void setUp() {
                    setTPChangeMockResult(doneEventObservable(changedTPEvent));

                    setTPObservableCall.run();
                }

                @Test
                public void testSetTPOnChangeUtilHasBeenCalledCorrect() {
                    verify(orderChangeUtilMock).setTakeProfitPrice(orderUnderTest, newTP);
                }

                @Test
                public void testSubscriberHasBeenNotifiedWithOrderEvent() {
                    assertOrderEventNotification(changedTPEvent);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }
        }
    }

    public class MergeSetup {

        private final String mergeOrderLabel = "MergeLabel";
        private final Set<IOrder> toMergeOrders = Sets.newHashSet(IOrderForTest.buyOrderEURUSD(),
                                                                  IOrderForTest.sellOrderEURUSD());
        private final Runnable mergeObservableCall =
                () -> positionSingleTask.mergeObservable(mergeOrderLabel, toMergeOrders)
                        .subscribe(taskSubscriber);

        public class TPIsNew {

            private final OrderEvent mergeEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.MERGE_OK);
            private final OrderEvent rejectEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.MERGE_REJECTED);

            private void mergeCreateMockResult(final Observable<OrderEvent> observable) {
                when(orderCreateUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                        .thenReturn(observable);
            }

            @Test
            public void testSubscriberNotYetCompletedWhenChangeUtilIsBusy() {
                mergeCreateMockResult(busyObservable());

                mergeObservableCall.run();

                taskSubscriber.assertNotCompleted();
            }

            public class MergeWithJFException {

                @Before
                public void setUp() {
                    mergeCreateMockResult(exceptionObservable());

                    mergeObservableCall.run();
                }

                @Test
                public void testMergeOnChangeUtilHasBeenCalledWithoutRetry() {
                    verify(orderCreateUtilMock).mergeOrders(mergeOrderLabel, toMergeOrders);
                }

                @Test
                public void testSubscriberGetsJFExceptionNotification() {
                    assertJFException(taskSubscriber);
                }
            }

            public class MergeCallWhichExceedsRetries {

                @Before
                public void setUp() {
                    setRetryExceededMock(() -> orderCreateUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders),
                                         rejectEvent);

                    mergeObservableCall.run();

                    rxTestUtil.advanceTimeForAllOrderRetries();
                }

                @Test
                public void testMergeCalledWithAllRetries() {
                    verify(orderCreateUtilMock, times(retryExceedCount))
                            .mergeOrders(mergeOrderLabel, toMergeOrders);
                }

                @Test
                public void testSubscriberGetsRejectExceptionNotification() {
                    assertRejectException(taskSubscriber);
                }
            }

            public class MergeCallWithFullRetriesThenSuccess {

                @Before
                public void setUp() {
                    setFullRetryMock(() -> orderCreateUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders),
                                     rejectEvent);

                    mergeObservableCall.run();

                    rxTestUtil.advanceTimeForAllOrderRetries();
                }

                @Test
                public void testMergeCalledWithAllRetries() {
                    verify(orderCreateUtilMock, times(retryExceedCount))
                            .mergeOrders(mergeOrderLabel, toMergeOrders);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }

            public class MergeOKCall {

                @Before
                public void setUp() {
                    mergeCreateMockResult(doneEventObservable(mergeEvent));

                    mergeObservableCall.run();
                }

                @Test
                public void testMergeOnChangeUtilHasBeenCalledCorrect() {
                    verify(orderCreateUtilMock).mergeOrders(mergeOrderLabel, toMergeOrders);
                }

                @Test
                public void testSubscriberHasBeenNotifiedWithOrderEvent() {
                    assertOrderEventNotification(mergeEvent);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }
        }
    }

    public class CloseSetup {

        public class OrderIsAlreadyClosed {

            @Before
            public void setUp() {
                orderUnderTest.setState(IOrder.State.CLOSED);

                positionSingleTask.closeObservable(orderUnderTest)
                        .subscribe(taskSubscriber);
            }

            @Test
            public void testNoCallToChangeUtil() {
                verify(orderChangeUtilMock, never()).close(orderUnderTest);
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class OrderIsFilled {

            private final OrderEvent closeOKEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.CLOSE_OK);
            private final OrderEvent rejectEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.CLOSE_REJECTED);
            private final Runnable closeCall =
                    () -> positionSingleTask.closeObservable(orderUnderTest).subscribe(taskSubscriber);

            private void setCloseMockResult(final Observable<OrderEvent> observable) {
                when(orderChangeUtilMock.close(orderUnderTest))
                        .thenReturn(observable);
            }

            @Before
            public void setUp() {
                orderUnderTest.setState(IOrder.State.FILLED);
            }

            @Test
            public void testSubscriberNotYetCompletedWhenChangeUtilIsBusy() {
                setCloseMockResult(busyObservable());

                closeCall.run();

                taskSubscriber.assertNotCompleted();
            }

            public class CloseWithJFException {

                @Before
                public void setUp() {
                    setCloseMockResult(exceptionObservable());

                    closeCall.run();
                }

                @Test
                public void testCloseOnChangeUtilHasBeenCalledWithoutRetry() {
                    verify(orderChangeUtilMock).close(orderUnderTest);
                }

                @Test
                public void testSubscriberGetsJFExceptionNotification() {
                    assertJFException(taskSubscriber);
                }
            }

            public class CloseCallWhichExceedsRetries {

                @Before
                public void setUp() {
                    setRetryExceededMock(() -> orderChangeUtilMock.close(orderUnderTest),
                                         rejectEvent);

                    closeCall.run();

                    rxTestUtil.advanceTimeForAllOrderRetries();
                }

                @Test
                public void testCloseCalledWithAllRetries() {
                    verify(orderChangeUtilMock, times(retryExceedCount))
                            .close(orderUnderTest);
                }

                @Test
                public void testSubscriberGetsRejectExceptionNotification() {
                    assertRejectException(taskSubscriber);
                }
            }

            public class CloseCallWithFullRetriesThenSuccess {

                @Before
                public void setUp() {
                    setFullRetryMock(() -> orderChangeUtilMock.close(orderUnderTest),
                                     rejectEvent);

                    closeCall.run();

                    rxTestUtil.advanceTimeForAllOrderRetries();
                }

                @Test
                public void testCloseCalledWithAllRetries() {
                    verify(orderChangeUtilMock, times(retryExceedCount))
                            .close(orderUnderTest);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }

            public class CloseOKCall {

                @Before
                public void setUp() {
                    setCloseMockResult(doneEventObservable(closeOKEvent));

                    closeCall.run();
                }

                @Test
                public void testCloseOnChangeUtilHasBeenCalledCorrect() {
                    verify(orderChangeUtilMock).close(orderUnderTest);
                }

                @Test
                public void testSubscriberHasBeenNotifiedWithOrderEvent() {
                    assertOrderEventNotification(closeOKEvent);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }
        }
    }
}
