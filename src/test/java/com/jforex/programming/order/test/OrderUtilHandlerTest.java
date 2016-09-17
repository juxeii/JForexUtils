package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderTaskData;
import com.jforex.programming.order.OrderTaskDataFactory;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilHandlerTest extends InstrumentUtilForTest {

    private OrderUtilHandler orderUtilHandler;

    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Mock
    private OrderTaskDataFactory orderTaskDataFactoryMock;
    @Mock
    private OrderTaskData orderTaskDataMock;
    @Captor
    private ArgumentCaptor<OrderCallRequest> callRequestCaptor;
    private final IOrder orderForTest = buyOrderEURUSD;
    private final OrderCallReason orderCallReason = OrderCallReason.SUBMIT;
    private final Subject<OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        setUpMocks();

        orderUtilHandler = new OrderUtilHandler(orderEventGatewayMock, new OrderTaskDataFactory());
    }

    public void setUpMocks() {
        when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);

        when(orderTaskDataMock.order()).thenReturn(orderForTest);
        when(orderTaskDataMock.callReason()).thenReturn(orderCallReason);
        when(orderTaskDataMock.isEventTypeForTask(OrderEventType.SUBMIT_OK))
            .thenReturn(true);
        when(orderTaskDataMock.isFinishEventType(OrderEventType.FULLY_FILLED))
            .thenReturn(true);
        when(orderTaskDataMock.isRejectEventType(OrderEventType.SUBMIT_REJECTED))
            .thenReturn(true);

        when(orderTaskDataFactoryMock.forCallReason(orderForTest, orderCallReason))
            .thenReturn(orderTaskDataMock);
    }

    private OrderEvent sendOrderEvent(final IOrder order,
                                      final OrderEventType orderEventType) {
        final OrderEvent orderEvent = new OrderEvent(order,
                                                     orderEventType,
                                                     true);
        orderEventSubject.onNext(orderEvent);
        return orderEvent;
    }

    public class OnCall {

        private Observable<OrderEvent> observable;

        @Before
        public void setUp() {
            observable = orderUtilHandler.callObservable(orderForTest, orderCallReason);
        }

        @Test
        public void noCallToOrderEventGateway() {
            verifyZeroInteractions(orderEventGatewayMock);
        }

        public class OnSubscribe {

            private TestObserver<OrderEvent> testObserver;

            private void assertNoEventsReceived() {
                testObserver.assertNoValues();
                testObserver.assertNotComplete();
            }

            @Before
            public void setUp() {
                testObserver = observable.test();
            }

            @Test
            public void orderIsRegisteredWithCorrectCallRequest() {
                verify(orderEventGatewayMock).registerOrderCallRequest(callRequestCaptor.capture());

                final OrderCallRequest callRequest = callRequestCaptor.getValue();
                assertThat(callRequest.order(), equalTo(orderForTest));
                assertThat(callRequest.reason(), equalTo(orderCallReason));
            }

            @Test
            public void orderIsRegisteredBeforeSubscriptionToEventGateway() {
                final InOrder inOrder = inOrder(orderEventGatewayMock);

                inOrder.verify(orderEventGatewayMock).registerOrderCallRequest(callRequestCaptor.capture());
                inOrder.verify(orderEventGatewayMock).observable();
            }

            @Test
            public void noEventsReceived() {
                assertNoEventsReceived();
            }

            @Test
            public void externalOrderInEventIsIgnored() {
                sendOrderEvent(buyOrderAUDUSD, OrderEventType.SUBMIT_OK);

                assertNoEventsReceived();
            }

            @Test
            public void notRegisteredEventTypeIsIgnored() {
                sendOrderEvent(orderForTest, OrderEventType.CHANGE_GTT_REJECTED);

                assertNoEventsReceived();
            }

            public class OnSubmitEvent {

                private OrderEvent submitEvent;

                @Before
                public void setUp() {
                    submitEvent = sendOrderEvent(orderForTest, OrderEventType.SUBMIT_OK);
                }

                @Test
                public void submitEventReceived() {
                    testObserver.assertValue(submitEvent);
                }

                @Test
                public void observableNotYetCompleted() {
                    testObserver.assertNotComplete();
                }

                public class OnFillEvent {

                    private OrderEvent fillEvent;

                    @Before
                    public void setUp() {
                        fillEvent = sendOrderEvent(orderForTest, OrderEventType.FULLY_FILLED);
                    }

                    @Test
                    public void submitEventReceived() {
                        testObserver.assertValues(submitEvent, fillEvent);
                    }

                    @Test
                    public void observableCompleted() {
                        testObserver.assertComplete();
                    }
                }
            }
        }
    }
}
