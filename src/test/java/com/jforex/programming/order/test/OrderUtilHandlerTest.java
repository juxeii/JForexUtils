package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilHandlerTest extends InstrumentUtilForTest {

    private OrderUtilHandler orderUtilHandler;

    @Mock
    private OrderCallExecutor orderCallExecutorMock;
    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Captor
    private ArgumentCaptor<Callable<IOrder>> orderCallCaptor;
    @Captor
    private ArgumentCaptor<OrderCallRequest> callRequestCaptor;
    private final IOrderForTest orderToClose = IOrderForTest.buyOrderEURUSD();
    private final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
    private final OrderCallCommand command = new CloseCommand(orderToClose);
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        setUpMocks();

        orderUtilHandler = new OrderUtilHandler(orderCallExecutorMock, orderEventGatewayMock);
    }

    public void setUpMocks() {
        setStrategyThread();
        orderToClose.setState(IOrder.State.FILLED);
        when(orderCallExecutorMock.callObservable(any()))
                .thenReturn(Observable.fromCallable(command.callable()));
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        orderEventSubject.onNext(new OrderEvent(order, orderEventType));
    }

    @Test
    public void rejectEventIsTreatedAsError() {
        final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
        final OrderEvent rejectEvent = new OrderEvent(IOrderForTest.buyOrderEURUSD(),
                                                      OrderEventType.CHANGE_AMOUNT_REJECTED);

        orderUtilHandler
                .rejectAsErrorObservable(rejectEvent)
                .subscribe(subscriber);

        subscriber.assertError(OrderCallRejectException.class);
    }

    @Test
    public void nonRejectEventIsTreatedAsNonError() {
        final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
        final OrderEvent nonRejectEvent = new OrderEvent(IOrderForTest.buyOrderEURUSD(),
                                                         OrderEventType.CHANGED_AMOUNT);

        orderUtilHandler
                .rejectAsErrorObservable(nonRejectEvent)
                .subscribe(subscriber);

        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    public class CloseCallSetup {

        private final Runnable closeCall =
                () -> orderUtilHandler.callObservable(command).subscribe(subscriber);

        @Before
        public void setUp() {
            when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
        }

        public class ExecutesWithJFExceptionError {

            @Before
            public void setUp() throws JFException {
                Mockito.doThrow(jfException).when(orderToClose).close();

                closeCall.run();
            }

            @Test
            public void subscriberCompletesWithJFError() {
                subscriber.assertValueCount(0);
                subscriber.assertError(JFException.class);
            }

            @Test
            public void testOrderIsNotRegisteredAtGateway() {
                verify(orderEventGatewayMock, never())
                        .registerOrderCallRequest(any());
            }
        }

        public class ExecutesOK {

            @Before
            public void setUp() {
                closeCall.run();
            }

            @Test
            public void closeCallIsExecutedOnSubscribe() throws Exception {
                verify(orderToClose).close();
            }

            @Test
            public void subscriberNotYetCompletedWhenNoEventWasSent() {
                subscriber.assertNotCompleted();
            }

            @Test
            public void noNotificationIfUnsubscribedEarly() {
                subscriber.unsubscribe();

                sendOrderEvent(orderToClose, OrderEventType.CLOSE_OK);

                subscriber.assertValueCount(0);
            }

            @Test
            public void onPartialCloseSubscriberIsNotCompleted() {
                sendOrderEvent(orderToClose, OrderEventType.PARTIAL_CLOSE_OK);

                subscriber.assertValueCount(1);
                subscriber.assertNotCompleted();
            }

            @Test
            public void testOrderRegisteredAtGateway() throws Exception {
                verify(orderEventGatewayMock)
                        .registerOrderCallRequest(callRequestCaptor.capture());

                final OrderCallRequest callRequest = callRequestCaptor.getValue();
                assertThat(callRequest.order(), equalTo(orderToClose));
                assertThat(callRequest.reason(), equalTo(OrderCallReason.CLOSE));
            }

            @Test
            public void subscriberCompletesOnDoneEvent() {
                sendOrderEvent(orderToClose, OrderEventType.CLOSE_OK);

                subscriber.assertNoErrors();
                subscriber.assertCompleted();
            }

            @Test
            public void subscriberCompletesOnRejectEvent() {
                sendOrderEvent(orderToClose, OrderEventType.CLOSE_REJECTED);

                subscriber.assertNoErrors();
                subscriber.assertValueCount(1);
                subscriber.assertCompleted();
            }

            @Test
            public void noMoreNotificationsAfterFinishEvent() {
                sendOrderEvent(orderToClose, OrderEventType.CLOSE_OK);
                subscriber.assertValueCount(1);

                sendOrderEvent(orderToClose, OrderEventType.CLOSE_OK);
                subscriber.assertValueCount(1);

                subscriber.assertNoErrors();
                subscriber.assertCompleted();
            }

            @Test
            public void eventOfOtherOrderIsIgnored() {
                sendOrderEvent(IOrderForTest.orderAUDUSD(), OrderEventType.CLOSE_OK);

                subscriber.assertNotCompleted();
            }

            @Test
            public void unknownOrderEventIsIgnored() {
                sendOrderEvent(orderToClose, OrderEventType.CHANGED_GTT);

                subscriber.assertNotCompleted();
            }
        }
    }

    public class CloseCallWithRetriesSetup {

        private final int maxRetriesCount = platformSettings.maxRetriesOnOrderFail();
        private final int retryExceedCount = maxRetriesCount + 1;
        private final long delayOnOrderFailRetry = platformSettings.delayOnOrderFailRetry();
        private final Runnable closeWithRetryCall =
                () -> orderUtilHandler.callWithRetryObservable(command).subscribe(subscriber);

        private void sendFailAndAdvanceTime(final int times) {
            for (int i = 0; i < times; ++i) {
                sendOrderEvent(orderToClose, OrderEventType.CLOSE_REJECTED);
                rxTestUtil.advanceTimeBy(delayOnOrderFailRetry, TimeUnit.MILLISECONDS);
            }
        }

        @Before
        public void setUp() {
            when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
        }

        @Test
        public void subscriberErrorsOnJFException() throws JFException {
            Mockito.doThrow(jfException).when(orderToClose).close();

            closeWithRetryCall.run();

            verify(orderToClose).close();
            subscriber.assertError(JFException.class);
        }

        public class ExecutesOK {

            @Before
            public void setUp() {
                closeWithRetryCall.run();
            }

            @Test
            public void subscriberCompletesAfterAllRetries() throws Exception {
                sendFailAndAdvanceTime(maxRetriesCount);
                sendOrderEvent(orderToClose, OrderEventType.CLOSE_OK);

                verify(orderToClose, times(maxRetriesCount + 1)).close();

                subscriber.assertNoErrors();
                subscriber.assertCompleted();
            }

            @Test
            public void subscriberErrorsAfterRetryExceed() throws Exception {
                sendFailAndAdvanceTime(retryExceedCount);

                verify(orderToClose, times(maxRetriesCount + 1)).close();

                subscriber.assertError(OrderCallRejectException.class);
            }
        }
    }
}
