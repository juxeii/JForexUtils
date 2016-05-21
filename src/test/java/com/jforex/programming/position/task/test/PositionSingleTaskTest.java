package com.jforex.programming.position.task.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.task.PositionRetryTask;
import com.jforex.programming.position.task.PositionSingleTask;
import com.jforex.programming.test.common.PositionCommonTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;

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
    @Mock
    private PositionRetryTask<IOrder> orderRetryTaskMock;
    @Mock
    private PositionRetryTask<String> mergeRetryTaskMock;
    @Captor
    private ArgumentCaptor<Supplier<Observable<OrderEvent>>> orderCallCaptor;
    @Captor
    private ArgumentCaptor<Predicate<IOrder>> predicateCaptor;
    @Captor
    private ArgumentCaptor<Predicate<String>> mergePredicateCaptor;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final Observable<OrderEvent> testObservable = Observable.empty();
    private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrder, sellOrder);
    private final String mergeOrderLabel = "MergeLabel";

    private void setOrderRetryTaskResultObservable(final Observable<OrderEvent> observable) {
        when(orderRetryTaskMock.create(orderCallCaptor.capture(),
                                       predicateCaptor.capture(),
                                       eq(orderUnderTest)))
                                               .thenReturn(observable);
    }

    private void setMergeRetryTaskResultObservable(final Observable<OrderEvent> observable) {
        when(mergeRetryTaskMock.create(orderCallCaptor.capture(),
                                       mergePredicateCaptor.capture(),
                                       eq(mergeOrderLabel)))
                                               .thenReturn(observable);
    }

    private void verifyRetryTaskCall() {
        verify(orderRetryTaskMock).create(orderCallCaptor.capture(),
                                          predicateCaptor.capture(),
                                          eq(orderUnderTest));
    }

    private void verifyMergeRetryTaskCall() {
        verify(mergeRetryTaskMock).create(orderCallCaptor.capture(),
                                          mergePredicateCaptor.capture(),
                                          eq(mergeOrderLabel));
    }

    private void assertOrderEventNotification(final OrderEvent expectedEvent) {
        taskSubscriber.assertValueCount(1);

        final OrderEvent orderEvent = taskSubscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(expectedEvent.order()));
        assertThat(orderEvent.type(), equalTo(expectedEvent.type()));
    }

    @Before
    public void setUp() {
        initCommonTestFramework();

        positionSingleTask = new PositionSingleTask(orderCreateUtilMock,
                                                    orderChangeUtilMock,
                                                    orderRetryTaskMock,
                                                    mergeRetryTaskMock);
    }

    public class SetSLSetup {

        private final static double newSL = 1.10123;

        private final Runnable setSLObservableCall =
                () -> positionSingleTask.setSLObservable(orderUnderTest, newSL).subscribe(taskSubscriber);

        @Before
        public void setUp() {
            when(orderChangeUtilMock.setStopLossPrice(orderUnderTest, newSL)).thenReturn(testObservable);

            orderUnderTest.setState(IOrder.State.FILLED);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            setOrderRetryTaskResultObservable(Observable.never());

            setSLObservableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class PredicateIsFalse {

            @Before
            public void setUp() {
                setOrderRetryTaskResultObservable(Observable.never());

                positionSingleTask.setSLObservable(orderUnderTest, orderUnderTest.getStopLossPrice())
                        .subscribe(taskSubscriber);
            }

            @Test
            public void testPredicateIsFalseWhenSLAlreadySet() {
                verifyRetryTaskCall();

                assertFalse(predicateCaptor.getValue().test(orderUnderTest));
            }
        }

        public class RetryTaskCallWithJFException {

            @Before
            public void setUp() {
                setOrderRetryTaskResultObservable(exceptionObservable());

                setSLObservableCall.run();
            }

            @Test
            public void testSetSLOnOrderHasBeenCalledWithoutRetry() {
                verifyRetryTaskCall();
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }

        public class RetryTaskCallOK {

            final OrderEvent changedSLEvent = new OrderEvent(orderUnderTest, OrderEventType.SL_CHANGE_OK);

            @Before
            public void setUp() {
                setOrderRetryTaskResultObservable(Observable.just(changedSLEvent));

                setSLObservableCall.run();
            }

            @Test
            public void testSetSLOnChangeUtilHasBeenCalledCorrect() {
                verifyRetryTaskCall();

                assertThat(orderCallCaptor.getValue().get(), equalTo(testObservable));
                assertTrue(predicateCaptor.getValue().test(orderUnderTest));
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

    public class SetTPSetup {

        private final static double newTP = 1.12123;

        private final Runnable setTPObservableCall =
                () -> positionSingleTask.setTPObservable(orderUnderTest, newTP).subscribe(taskSubscriber);

        @Before
        public void setUp() {
            when(orderChangeUtilMock.setTakeProfitPrice(orderUnderTest, newTP)).thenReturn(testObservable);

            orderUnderTest.setState(IOrder.State.FILLED);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            setOrderRetryTaskResultObservable(Observable.never());

            setTPObservableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class PredicateIsFalse {

            @Before
            public void setUp() {
                setOrderRetryTaskResultObservable(Observable.never());

                positionSingleTask.setTPObservable(orderUnderTest, orderUnderTest.getTakeProfitPrice())
                        .subscribe(taskSubscriber);
            }

            @Test
            public void testPredicateIsFalseWhenTPAlreadySet() {
                verifyRetryTaskCall();

                assertFalse(predicateCaptor.getValue().test(orderUnderTest));
            }
        }

        public class RetryTaskCallWithJFException {

            @Before
            public void setUp() {
                setOrderRetryTaskResultObservable(exceptionObservable());

                setTPObservableCall.run();
            }

            @Test
            public void testSetTPOnOrderHasBeenCalledWithoutRetry() {
                verifyRetryTaskCall();
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }

        public class RetryTaskCallOK {

            final OrderEvent changedTPEvent = new OrderEvent(orderUnderTest, OrderEventType.TP_CHANGE_OK);

            @Before
            public void setUp() {
                setOrderRetryTaskResultObservable(Observable.just(changedTPEvent));

                setTPObservableCall.run();
            }

            @Test
            public void testSetTPOnChangeUtilHasBeenCalledCorrect() {
                verifyRetryTaskCall();

                assertThat(orderCallCaptor.getValue().get(), equalTo(testObservable));
                assertTrue(predicateCaptor.getValue().test(orderUnderTest));
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

    public class CloseSetup {

        private final Runnable closeObservableCall =
                () -> positionSingleTask.closeObservable(orderUnderTest).subscribe(taskSubscriber);

        @Before
        public void setUp() {
            when(orderChangeUtilMock.close(orderUnderTest)).thenReturn(testObservable);

            orderUnderTest.setState(IOrder.State.FILLED);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            setOrderRetryTaskResultObservable(Observable.never());

            closeObservableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class PredicateIsFalse {

            @Before
            public void setUp() {
                setOrderRetryTaskResultObservable(Observable.never());

                orderUnderTest.setState(IOrder.State.CLOSED);

                closeObservableCall.run();
            }

            @Test
            public void testPredicateIsFalseWhenAlreadyClosed() {
                verifyRetryTaskCall();

                assertFalse(predicateCaptor.getValue().test(orderUnderTest));
            }
        }

        public class RetryTaskCallWithJFException {

            @Before
            public void setUp() {
                setOrderRetryTaskResultObservable(exceptionObservable());

                closeObservableCall.run();
            }

            @Test
            public void testCloseOnOrderHasBeenCalledWithoutRetry() {
                verifyRetryTaskCall();
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }

        public class RetryTaskCallOK {

            final OrderEvent closeEvent = new OrderEvent(orderUnderTest, OrderEventType.CLOSE_OK);

            @Before
            public void setUp() {
                setOrderRetryTaskResultObservable(Observable.just(closeEvent));

                closeObservableCall.run();
            }

            @Test
            public void testCloseOnChangeUtilHasBeenCalledCorrect() {
                verifyRetryTaskCall();

                assertThat(orderCallCaptor.getValue().get(), equalTo(testObservable));
                assertTrue(predicateCaptor.getValue().test(orderUnderTest));
            }

            @Test
            public void testSubscriberHasBeenNotifiedWithOrderEvent() {
                assertOrderEventNotification(closeEvent);
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }
    }

    public class MergeSetup {

        private final Runnable mergeObservableCall =
                () -> positionSingleTask.mergeObservable(mergeOrderLabel, toMergeOrders)
                        .subscribe(taskSubscriber);

        @Before
        public void setUp() {
            when(orderCreateUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                    .thenReturn(testObservable);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            setMergeRetryTaskResultObservable(Observable.never());

            mergeObservableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class RetryTaskCallWithJFException {

            @Before
            public void setUp() {
                setMergeRetryTaskResultObservable(exceptionObservable());

                mergeObservableCall.run();
            }

            @Test
            public void testMergeHasBeenCalledWithoutRetry() {
                verifyMergeRetryTaskCall();
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }

        public class RetryTaskCallOK {

            final OrderEvent mergeEvent = new OrderEvent(orderUnderTest, OrderEventType.MERGE_OK);

            @Before
            public void setUp() {
                setMergeRetryTaskResultObservable(Observable.just(mergeEvent));

                mergeObservableCall.run();
            }

            @Test
            public void testMergeHasBeenCalledCorrect() {
                verifyMergeRetryTaskCall();

                assertThat(orderCallCaptor.getValue().get(), equalTo(testObservable));
                assertTrue(mergePredicateCaptor.getValue().test(mergeOrderLabel));
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
