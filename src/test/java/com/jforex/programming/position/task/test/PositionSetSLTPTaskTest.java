package com.jforex.programming.position.task.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionRetryLogic;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.position.task.PositionSetSLTPTask;
import com.jforex.programming.test.common.PositionCommonTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionSetSLTPTaskTest extends PositionCommonTest {

    private PositionSetSLTPTask positionSetSLTPTask;

    @Mock
    private OrderChangeUtil orderChangeUtilMock;
    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    private Position position;
    private final PositionRetryLogic positionRetryLogic = new PositionRetryLogic();
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();

    private final static double newSL = 1.10123;
    private final static double newTP = 1.12123;
    private final static double noSL = platformSettings.noSLPrice();
    private final static double noTP = platformSettings.noTPPrice();

    @Before
    public void setUp() {
        initCommonTestFramework();

        position = new Position(instrumentEURUSD, orderEventSubject);
        positionSetSLTPTask = new PositionSetSLTPTask(orderChangeUtilMock,
                                                      positionRetryLogic);
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        orderEventSubject.onNext(new OrderEvent(order, orderEventType));
    }

    public class SetSLSetup {

        private final Runnable setSLCompletableCall =
                () -> positionSetSLTPTask.setSLCompletable(orderUnderTest, newSL).subscribe(taskSubscriber);
        private final OrderEvent rejectEvent = new OrderEvent(orderUnderTest, OrderEventType.CHANGE_SL_REJECTED);

        @Before
        public void setUp() {
            orderUnderTest.setState(IOrder.State.FILLED);

            position.addOrder(orderUnderTest);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            when(orderChangeUtilMock.setStopLossPrice(orderUnderTest, newSL)).thenReturn(Observable.never());

            setSLCompletableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class WhenNoSLPresent {

            @Before
            public void setUp() {
                orderUnderTest.setStopLossPrice(noSL);

                setSLCompletableCall.run();
            }

            @Test
            public void testNoCallToChangeutil() {
                verify(orderChangeUtilMock, never()).setStopLossPrice(orderUnderTest, noSL);
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class SetSLWithJFException {

            @Before
            public void setUp() {
                when(orderChangeUtilMock.setStopLossPrice(orderUnderTest, newSL))
                        .thenReturn(Observable.error(jfException));

                setSLCompletableCall.run();
            }

            @Test
            public void testSetSLOnOrderHasBeenCalledWithoutRetry() {
                verify(orderChangeUtilMock).setStopLossPrice(orderUnderTest, newSL);
            }

            @Test
            public void testPositionHasStillOrder() {
                assertTrue(position.contains(orderUnderTest));
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }

        public class SetSLRejectWhichExceedsRetries {

            @Before
            public void setUp() {
                setRetryExceededMock(() -> orderChangeUtilMock.setStopLossPrice(orderUnderTest, newSL),
                                     rejectEvent);

                setSLCompletableCall.run();

                rxTestUtil.advanceTimeForAllOrderRetries();
            }

            @Test
            public void testSetSLCalledWithAllRetries() {
                verify(orderChangeUtilMock, times(retryExceedCount)).setStopLossPrice(orderUnderTest, newSL);
            }

            @Test
            public void testPositionHasOrder() {
                assertTrue(position.contains(orderUnderTest));
            }

            @Test
            public void testSubscriberGetsRejectExceptionNotification() {
                assertRejectException(taskSubscriber);
            }
        }

        public class SetSLRejectWithFullRetriesThenSuccess {

            @Before
            public void setUp() {
                setFullRetryMock(() -> orderChangeUtilMock.setStopLossPrice(orderUnderTest, newSL),
                                 rejectEvent);

                setSLCompletableCall.run();

                rxTestUtil.advanceTimeForAllOrderRetries();

                sendOrderEvent(orderUnderTest, OrderEventType.SL_CHANGE_OK);
            }

            @Test
            public void testSetSLCalledWithAllRetries() {
                verify(orderChangeUtilMock, times(retryExceedCount)).setStopLossPrice(orderUnderTest, newSL);
            }

            @Test
            public void testPositionHasOrder() {
                assertTrue(position.contains(orderUnderTest));
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class SetSLOK {

            @Before
            public void setUp() {
                final OrderEvent changedSLEvent = new OrderEvent(orderUnderTest, OrderEventType.SL_CHANGE_OK);

                when(orderChangeUtilMock.setStopLossPrice(orderUnderTest, newSL))
                        .thenReturn(Observable.just(changedSLEvent));

                setSLCompletableCall.run();
                sendOrderEvent(orderUnderTest, OrderEventType.SL_CHANGE_OK);
            }

            @Test
            public void testSetSLOnChangeUtilHasBeenCalled() {
                verify(orderChangeUtilMock).setStopLossPrice(orderUnderTest, newSL);
            }

            @Test
            public void testPositionHasOrder() {
                assertTrue(position.contains(orderUnderTest));
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }
    }

    public class SetTPSetup {

        private final Runnable setTPCompletableCall =
                () -> positionSetSLTPTask.setTPCompletable(orderUnderTest, newTP).subscribe(taskSubscriber);
        private final OrderEvent rejectEvent = new OrderEvent(orderUnderTest, OrderEventType.CHANGE_TP_REJECTED);

        @Before
        public void setUp() {
            orderUnderTest.setState(IOrder.State.FILLED);

            position.addOrder(orderUnderTest);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            when(orderChangeUtilMock.setTakeProfitPrice(orderUnderTest, newTP)).thenReturn(Observable.never());

            setTPCompletableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class WhenNoTPPresent {

            @Before
            public void setUp() {
                orderUnderTest.setTakeProfitPrice(noTP);

                setTPCompletableCall.run();
            }

            @Test
            public void testNoCallToChangeutil() {
                verify(orderChangeUtilMock, never()).setTakeProfitPrice(orderUnderTest, noTP);
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class SetTPWithJFException {

            @Before
            public void setUp() {
                when(orderChangeUtilMock.setTakeProfitPrice(orderUnderTest, newTP))
                        .thenReturn(Observable.error(jfException));

                setTPCompletableCall.run();
            }

            @Test
            public void testSetTPOnOrderHasBeenCalledWithoutRetry() {
                verify(orderChangeUtilMock).setTakeProfitPrice(orderUnderTest, newTP);
            }

            @Test
            public void testPositionHasStillOrder() {
                assertTrue(position.contains(orderUnderTest));
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }

        public class SetTPRejectWhichExceedsRetries {

            @Before
            public void setUp() {
                setRetryExceededMock(() -> orderChangeUtilMock.setTakeProfitPrice(orderUnderTest, newTP),
                                     rejectEvent);

                setTPCompletableCall.run();

                rxTestUtil.advanceTimeForAllOrderRetries();
            }

            @Test
            public void testSetTPCalledWithAllRetries() {
                verify(orderChangeUtilMock, times(retryExceedCount)).setTakeProfitPrice(orderUnderTest, newTP);
            }

            @Test
            public void testPositionHasOrder() {
                assertTrue(position.contains(orderUnderTest));
            }

            @Test
            public void testSubscriberGetsRejectExceptionNotification() {
                assertRejectException(taskSubscriber);
            }
        }

        public class SetTPRejectWithFullRetriesThenSuccess {

            @Before
            public void setUp() {
                setFullRetryMock(() -> orderChangeUtilMock.setTakeProfitPrice(orderUnderTest, newTP),
                                 rejectEvent);

                setTPCompletableCall.run();

                rxTestUtil.advanceTimeForAllOrderRetries();

                sendOrderEvent(orderUnderTest, OrderEventType.TP_CHANGE_OK);
            }

            @Test
            public void testSetTPCalledWithAllRetries() {
                verify(orderChangeUtilMock, times(retryExceedCount)).setTakeProfitPrice(orderUnderTest, newTP);
            }

            @Test
            public void testPositionHasOrder() {
                assertTrue(position.contains(orderUnderTest));
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class SetSLOK {

            private final OrderEvent changedTPEvent = new OrderEvent(orderUnderTest, OrderEventType.TP_CHANGE_OK);

            @Before
            public void setUp() {

                when(orderChangeUtilMock.setTakeProfitPrice(orderUnderTest, newTP))
                        .thenReturn(Observable.just(changedTPEvent));

                setTPCompletableCall.run();
                sendOrderEvent(orderUnderTest, OrderEventType.TP_CHANGE_OK);
            }

            @Test
            public void testSetTPOnOrderHasBeenCalled() {
                verify(orderChangeUtilMock).setTakeProfitPrice(orderUnderTest, newTP);
            }

            @Test
            public void testPositionHasOrder() {
                assertTrue(position.contains(orderUnderTest));
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }
    }
}
