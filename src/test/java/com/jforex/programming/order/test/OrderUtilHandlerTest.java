package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.misc.TaskExecutor;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallCommand;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilHandlerTest extends InstrumentUtilForTest {

    private OrderUtilHandler orderUtilHandler;

    @Mock
    private TaskExecutor taskExecutorMock;
    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Captor
    private ArgumentCaptor<Callable<IOrder>> orderCallCaptor;
    @Captor
    private ArgumentCaptor<OrderCallRequest> callRequestCaptor;
    private final IOrder orderToClose = buyOrderEURUSD;
    private final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
    private final OrderCallCommand command = new OrderCallCommand(() -> {
        orderToClose.close();
        return orderToClose;
    },
                                                                  OrderCallReason.CLOSE);
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        setUpMocks();

        orderUtilHandler = new OrderUtilHandler(taskExecutorMock, orderEventGatewayMock);
    }

    public void setUpMocks() {
        setStrategyThread();
        orderUtilForTest.setState(orderToClose, IOrder.State.FILLED);

        when(taskExecutorMock.onStrategyThread(any()))
            .thenReturn(Observable.fromCallable(command.callable()));
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        orderEventSubject.onNext(new OrderEvent(order, orderEventType));
    }

    public class CloseCallSetup {

        private final Runnable closeCall =
                () -> orderUtilHandler
                    .callObservable(command)
                    .subscribe(subscriber);

        @Before
        public void setUp() {
            when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
        }

        public class ExecutesWithJFExceptionError {

            @Before
            public void setUp() throws JFException {
                Mockito
                    .doThrow(jfException)
                    .when(orderToClose)
                    .close();

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
            public void subscriberErrosRejectEvent() {
                sendOrderEvent(orderToClose, OrderEventType.CLOSE_REJECTED);

                subscriber.assertError(OrderCallRejectException.class);
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
                sendOrderEvent(orderUtilForTest.sellOrderAUDUSD(), OrderEventType.CLOSE_OK);

                subscriber.assertNotCompleted();
            }

            @Test
            public void unknownOrderEventIsIgnored() {
                sendOrderEvent(orderToClose, OrderEventType.CHANGED_GTT);

                subscriber.assertNotCompleted();
            }
        }
    }
}
