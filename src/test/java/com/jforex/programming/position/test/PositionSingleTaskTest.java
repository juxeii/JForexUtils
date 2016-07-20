package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.OrderChangeCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.PositionSingleTask;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionSingleTaskTest extends CommonUtilForTest {

    private PositionSingleTask positionSingleTask;

    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Captor
    private ArgumentCaptor<Supplier<Observable<OrderEvent>>> orderCallCaptor;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();

    @Before
    public void setUp() {
        positionSingleTask = new PositionSingleTask(orderUtilHandlerMock);
    }

    private void assertOrderEventNotification(final OrderEvent expectedEvent) {
        taskSubscriber.assertValueCount(1);

        final OrderEvent orderEvent = taskSubscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(expectedEvent.order()));
        assertThat(orderEvent.type(), equalTo(expectedEvent.type()));
    }

    private void setOrderUtilMockResult(final Observable<OrderEvent> observable) {
        when(orderUtilHandlerMock.callWithRetryObservable(any()))
                .thenReturn(observable);
    }

    private void verfiyOrderUtilMockCall(final Class<? extends OrderCallCommand> clazz) {
        verify(orderUtilHandlerMock).callWithRetryObservable(any(clazz));
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
                positionSingleTask
                        .setSLObservable(orderUnderTest, orderSL)
                        .subscribe(taskSubscriber);
            }

            @Test
            public void testNoCallToOrderUtilHandlerMock() {
                verify(orderUtilHandlerMock, never()).callObservable(any());
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class SLIsNew {

            private final OrderEvent changedSLEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.CHANGED_SL);
            private static final double newSL = 1.10123;
            private final Runnable setSLObservableCall =
                    () -> positionSingleTask
                            .setSLObservable(orderUnderTest, newSL)
                            .subscribe(taskSubscriber);

            @Test
            public void testSubscriberNotYetCompletedWhenOrderUtilHandlerMockIsBusy() {
                setOrderUtilMockResult(busyObservable());

                setSLObservableCall.run();

                taskSubscriber.assertNotCompleted();
            }

            public class SetSLWithJFException {

                @Before
                public void setUp() {
                    setOrderUtilMockResult(exceptionObservable());

                    setSLObservableCall.run();
                }

                @Test
                public void testSetSLOnOrderUtilHandlerMockHasBeenCalledWithoutRetry() {
                    verfiyOrderUtilMockCall(OrderChangeCommand.class);
                }

                @Test
                public void testSubscriberGetsJFExceptionNotification() {
                    assertJFException(taskSubscriber);
                }
            }

            public class SetSLOKCall {

                @Before
                public void setUp() {
                    setOrderUtilMockResult(doneEventObservable(changedSLEvent));

                    setSLObservableCall.run();
                }

                @Test
                public void testSetSLOnOrderUtilHandlerMockHasBeenCalledCorrect() {
                    verfiyOrderUtilMockCall(SetSLCommand.class);
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
                positionSingleTask
                        .setTPObservable(orderUnderTest, orderTP)
                        .subscribe(taskSubscriber);
            }

            @Test
            public void testNoCallToOrderUtilHandlerMock() {
                verify(orderUtilHandlerMock, never()).callObservable(any());
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class TPIsNew {

            private final OrderEvent changedTPEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.CHANGED_TP);
            private static final double newTP = 1.12123;
            private final Runnable setTPObservableCall =
                    () -> positionSingleTask
                            .setTPObservable(orderUnderTest, newTP)
                            .subscribe(taskSubscriber);

            @Test
            public void testSubscriberNotYetCompletedWhenOrderUtilHandlerMockIsBusy() {
                setOrderUtilMockResult(busyObservable());

                setTPObservableCall.run();

                taskSubscriber.assertNotCompleted();
            }

            public class SetTPWithJFException {

                @Before
                public void setUp() {
                    setOrderUtilMockResult(exceptionObservable());

                    setTPObservableCall.run();
                }

                @Test
                public void testSetTPOnOrderUtilHandlerMockHasBeenCalledWithoutRetry() {
                    verfiyOrderUtilMockCall(SetTPCommand.class);
                }

                @Test
                public void testSubscriberGetsJFExceptionNotification() {
                    assertJFException(taskSubscriber);
                }
            }

            public class SetTPOKCall {

                @Before
                public void setUp() {
                    setOrderUtilMockResult(doneEventObservable(changedTPEvent));

                    setTPObservableCall.run();
                }

                @Test
                public void testSetTPOnOrderUtilHandlerMockHasBeenCalledCorrect() {
                    verfiyOrderUtilMockCall(SetTPCommand.class);
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

    public class CloseSetup {

        public class OrderIsAlreadyClosed {

            @Before
            public void setUp() {
                orderUnderTest.setState(IOrder.State.CLOSED);

                positionSingleTask
                        .closeObservable(orderUnderTest)
                        .subscribe(taskSubscriber);
            }

            @Test
            public void testNoCallToOrderUtilHandlerMock() {
                verify(orderUtilHandlerMock, never()).callObservable(any());
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class OrderIsFilled {

            private final OrderEvent closeOKEvent =
                    new OrderEvent(orderUnderTest, OrderEventType.CLOSE_OK);
            private final Runnable closeCall =
                    () -> positionSingleTask
                            .closeObservable(orderUnderTest)
                            .subscribe(taskSubscriber);

            @Before
            public void setUp() {
                orderUnderTest.setState(IOrder.State.FILLED);
            }

            @Test
            public void testSubscriberNotYetCompletedWhenOrderUtilHandlerMockIsBusy() {
                setOrderUtilMockResult(busyObservable());

                closeCall.run();

                taskSubscriber.assertNotCompleted();
            }

            public class CloseWithJFException {

                @Before
                public void setUp() {
                    setOrderUtilMockResult(exceptionObservable());

                    closeCall.run();
                }

                @Test
                public void testCloseOnOrderUtilHandlerMockHasBeenCalledWithoutRetry() {
                    verfiyOrderUtilMockCall(CloseCommand.class);
                }

                @Test
                public void testSubscriberGetsJFExceptionNotification() {
                    assertJFException(taskSubscriber);
                }
            }

            public class CloseOKCall {

                @Before
                public void setUp() {
                    setOrderUtilMockResult(doneEventObservable(closeOKEvent));

                    closeCall.run();
                }

                @Test
                public void testCloseOnOrderUtilHandlerMockHasBeenCalledCorrect() {
                    verfiyOrderUtilMockCall(CloseCommand.class);
                }

                @Test
                public void subscriberHasBeenNotifiedWithOrderEvent() {
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
