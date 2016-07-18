package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

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
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        orderUtilHandler = new OrderUtilHandler(orderCallExecutorMock, orderEventGatewayMock);
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

        private final IOrderForTest orderToClose = IOrderForTest.buyOrderEURUSD();
        private final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
        private final OrderCallCommand command = new CloseCommand(orderToClose);
        private final Supplier<Observable<OrderEvent>> runCall =
                () -> orderUtilHandler.callObservable(command);

        @Before
        public void setUp() {
            when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
        }

        @Test
        public void closeCallIsExecutedOnSubscribe() throws Exception {
            when(orderCallExecutorMock.callObservable(any()))
                    .thenReturn(Observable.just(orderToClose));

            runCall.get().subscribe(subscriber);
            verify(orderCallExecutorMock).callObservable(orderCallCaptor.capture());
            orderCallCaptor.getValue().call();

            verify(orderToClose).close();
        }

        public class ExecutesWithJFExceptionError {

            @Before
            public void setUp() {
                when(orderCallExecutorMock.callObservable(any()))
                        .thenReturn(Observable.error(jfException));

                runCall.get().subscribe(subscriber);
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
                when(orderCallExecutorMock.callObservable(any()))
                        .thenReturn(Observable.just(orderToClose));

                runCall.get().subscribe(subscriber);
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
}
