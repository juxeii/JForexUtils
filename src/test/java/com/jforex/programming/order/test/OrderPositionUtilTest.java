package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderPositionUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.task.PositionBatchTask;
import com.jforex.programming.position.task.PositionMultiTask;
import com.jforex.programming.position.task.PositionSingleTask;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.common.PositionCommonTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class OrderPositionUtilTest extends PositionCommonTest {

    private OrderPositionUtil orderPositionUtil;

    @Mock
    private OrderCreateUtil orderCreateUtilMock;
    @Mock
    private PositionSingleTask positionSingleTaskMock;
    @Mock
    private PositionMultiTask positionMultiTaskMock;
    @Mock
    private PositionBatchTask positionBatchTaskMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionMock;
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
        setUpMocks();

        orderPositionUtil = new OrderPositionUtil(orderCreateUtilMock,
                                                  positionSingleTaskMock,
                                                  positionMultiTaskMock,
                                                  positionBatchTaskMock,
                                                  positionFactoryMock);
    }

    public void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
                .thenReturn(positionMock);
    }

    public class SubmitSetup {

        private final OrderEvent submitEvent =
                new OrderEvent(orderUnderTest, OrderEventType.FULL_FILL_OK);
        private final OrderEvent rejectEvent =
                new OrderEvent(orderUnderTest, OrderEventType.SUBMIT_REJECTED);
        private final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
        private final Runnable submitObservableCall =
                () -> orderPositionUtil.submitOrder(orderParams).subscribe(taskSubscriber);

        private void submitCreateMockResult(final Observable<OrderEvent> observable) {
            when(orderCreateUtilMock.submitOrder(orderParams))
                    .thenReturn(observable);
        }

        @Test
        public void testSubmitIsCalledAlsoWhenNotSubscribed() {
            submitCreateMockResult(doneEventObservable(submitEvent));

            orderPositionUtil.submitOrder(orderParams);

            verify(orderCreateUtilMock).submitOrder(orderParams);
            taskSubscriber.assertNotCompleted();
        }

        @Test
        public void testSubscriberNotYetCompletedWhenCreateUtilIsBusy() {
            submitCreateMockResult(busyObservable());

            submitObservableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class SubmitWithJFException {

            @Before
            public void setUp() {
                submitCreateMockResult(exceptionObservable());

                submitObservableCall.run();
            }

            @Test
            public void testSubmitCreateUtilHasBeenCalledWithoutRetry() {
                verify(orderCreateUtilMock).submitOrder(orderParams);
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }

        public class SubmitWithRejection {

            @Before
            public void setUp() {
                submitCreateMockResult(rejectObservable(rejectEvent));

                submitObservableCall.run();
            }

            @Test
            public void testSubmitCreateUtilHasBeenCalledWithoutRetry() {
                verify(orderCreateUtilMock).submitOrder(orderParams);
            }

            @Test
            public void testSubscriberGetsRejectExceptionNotification() {
                assertRejectException(taskSubscriber);
            }
        }

        public class SubmitOK {

            @Before
            public void setUp() {
                submitCreateMockResult(doneEventObservable(submitEvent));

                submitObservableCall.run();
            }

            @Test
            public void testSubmitOnCreateUtilHasBeenCalledCorrect() {
                verify(orderCreateUtilMock).submitOrder(orderParams);
            }

            @Test
            public void testSubscriberHasBeenNotifiedWithOrderEvent() {
                assertOrderEventNotification(submitEvent);
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }

            @Test
            public void testOrderHasBeenAddedToPosition() {
                verify(positionMock).addOrder(orderUnderTest);
            }
        }
    }

    public class MergeSetup {

        private final String mergeOrderLabel = "MergeLabel";
        private final Set<IOrder> toMergeOrders = Sets.newHashSet(IOrderForTest.buyOrderEURUSD(),
                                                                  IOrderForTest.sellOrderEURUSD());
        private final OrderEvent mergeEvent =
                new OrderEvent(orderUnderTest, OrderEventType.MERGE_OK);
        private final OrderEvent rejectEvent =
                new OrderEvent(orderUnderTest, OrderEventType.MERGE_REJECTED);
        private final Runnable mergeObservableCall =
                () -> orderPositionUtil.mergeOrders(mergeOrderLabel, toMergeOrders).subscribe(taskSubscriber);

        private void mergeCreateMockResult(final Observable<OrderEvent> observable) {
            when(orderCreateUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                    .thenReturn(observable);
        }

        @Test
        public void testMergeIsCalledAlsoWhenNotSubscribed() {
            mergeCreateMockResult(doneEventObservable(mergeEvent));

            orderPositionUtil.mergeOrders(mergeOrderLabel, toMergeOrders);

            verify(orderCreateUtilMock).mergeOrders(mergeOrderLabel, toMergeOrders);
            taskSubscriber.assertNotCompleted();
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

        public class MergeWithRejection {

            @Before
            public void setUp() {
                mergeCreateMockResult(rejectObservable(rejectEvent));

                mergeObservableCall.run();
            }

            @Test
            public void testMergeCreateUtilHasBeenCalledWithoutRetry() {
                verify(orderCreateUtilMock).mergeOrders(mergeOrderLabel, toMergeOrders);
            }

            @Test
            public void testSubscriberGetsRejectExceptionNotification() {
                assertRejectException(taskSubscriber);
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

            @Test
            public void testOrderHasBeenAddedToPosition() {
                verify(positionMock).addOrder(orderUnderTest);
            }
        }
    }

    public class ClosePositionSetup {

        private final Set<IOrder> ordersToClose = Sets.newHashSet(IOrderForTest.buyOrderEURUSD(),
                                                                  IOrderForTest.sellOrderEURUSD());
        private final Runnable closePositionCall =
                () -> orderPositionUtil.closePosition(instrumentEURUSD).subscribe(taskSubscriber);

        public class NoOrdersToClose {

            @Before
            public void setUp() {
                when(positionMock.filledOrOpenedOrders())
                        .thenReturn(Sets.newHashSet());

                closePositionCall.run();
            }

            @Test
            public void testNoCallToBatchUtil() {
                verify(positionBatchTaskMock, never()).closeCompletable(any());
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class WithOrdersToClose {

            private final OrderEvent closeOKEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.CLOSE_OK);
            private final OrderEvent rejectEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.CLOSE_REJECTED);

            private void setBatchMockResult(final Observable<OrderEvent> observable) {
                when(positionBatchTaskMock.closeCompletable(ordersToClose))
                        .thenReturn(observable.toCompletable());
            }

            @Before
            public void setUp() {
                when(positionMock.filledOrOpenedOrders())
                        .thenReturn(ordersToClose);
            }

            @Test
            public void testClosePositionIsCalledAlsoWhenNotSubscribed() {
                setBatchMockResult(doneEventObservable(closeOKEvent));

                orderPositionUtil.closePosition(instrumentEURUSD);

                verify(positionBatchTaskMock).closeCompletable(ordersToClose);
                taskSubscriber.assertNotCompleted();
            }

            @Test
            public void testSubscriberNotYetCompletedWhenChangeUtilIsBusy() {
                setBatchMockResult(busyObservable());

                closePositionCall.run();

                taskSubscriber.assertNotCompleted();
            }

            @Test
            public void testAllPositionOrdersAreMarkedActive() {
                setBatchMockResult(doneEventObservable(closeOKEvent));

                closePositionCall.run();

                verify(positionMock).markAllActive();
            }

            public class CloseWithJFException {

                @Before
                public void setUp() {
                    setBatchMockResult(exceptionObservable());

                    closePositionCall.run();
                }

                @Test
                public void testClosePositionOnBatchUtilHasBeenCalledWithoutRetry() {
                    verify(positionBatchTaskMock).closeCompletable(ordersToClose);
                }

                @Test
                public void testSubscriberGetsJFExceptionNotification() {
                    assertJFException(taskSubscriber);
                }
            }

            public class ClosePositionWithRejection {

                @Before
                public void setUp() {
                    setBatchMockResult(rejectObservable(rejectEvent));

                    closePositionCall.run();
                }

                @Test
                public void testClosePositionOnBatchUtilHasBeenCalledWithoutRetry() {
                    verify(positionBatchTaskMock).closeCompletable(ordersToClose);
                }

                @Test
                public void testSubscriberGetsRejectExceptionNotification() {
                    assertRejectException(taskSubscriber);
                }
            }

            public class ClosePositionOKCall {

                @Before
                public void setUp() {
                    setBatchMockResult(doneEventObservable(closeOKEvent));

                    closePositionCall.run();
                }

                @Test
                public void testClosePositionOnBatchUtilHasBeenCalledCorrect() {
                    verify(positionBatchTaskMock).closeCompletable(ordersToClose);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }
        }
    }
}