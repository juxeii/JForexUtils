package com.jforex.programming.order.test;

import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.order.event.OrderEventTypeDataFactory;
import com.jforex.programming.rx.JFHotPublisher;
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
    private OrderEventTypeDataFactory orderEventTypeDataFactory;
    @Mock
    private OrderEventTypeData orderEventTypeData;
    @Captor
    private ArgumentCaptor<OrderCallRequest> callRequestCaptor;
    private final JFHotPublisher<OrderCallRequest> callRequestPublisher = new JFHotPublisher<>();
    private final TestObserver<OrderCallRequest> requestObserver = callRequestPublisher.observable().test();
    private final IOrder orderForTest = buyOrderEURUSD;
    private final OrderCallReason orderCallReason = OrderCallReason.SUBMIT;
    private final EnumSet<OrderEventType> doneEventTypes = EnumSet.of(CLOSE_OK);
    private final EnumSet<OrderEventType> rejectEventTypes = EnumSet.of(CLOSE_REJECTED);
    private final EnumSet<OrderEventType> infoEventTypes = EnumSet.of(PARTIAL_CLOSE_OK);
    private final EnumSet<OrderEventType> allEventTypes = EnumSet.of(CLOSE_OK,
                                                                     CLOSE_REJECTED,
                                                                     PARTIAL_CLOSE_OK);
    private final EnumSet<OrderEventType> finishEventTypes = EnumSet.of(CLOSE_OK,
                                                                        CLOSE_REJECTED);
    private final Subject<OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        setUpMocks();

        orderUtilHandler = new OrderUtilHandler(orderEventGatewayMock,
                                                orderEventTypeDataFactory,
                                                callRequestPublisher);
    }

    public void setUpMocks() {
        when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);

        when(orderEventTypeData.doneEventTypes()).thenReturn(doneEventTypes);
        when(orderEventTypeData.rejectEventTypes()).thenReturn(rejectEventTypes);
        when(orderEventTypeData.infoEventTypes()).thenReturn(infoEventTypes);
        when(orderEventTypeData.allEventTypes()).thenReturn(allEventTypes);
        when(orderEventTypeData.finishEventTypes()).thenReturn(finishEventTypes);

        when(orderEventTypeDataFactory.forCallReason(orderCallReason))
            .thenReturn(orderEventTypeData);
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
                final OrderCallRequest callRequest = getOnNextEvent(requestObserver, 0);
                assertThat(callRequest.order(), equalTo(orderForTest));
                assertThat(callRequest.reason(), equalTo(orderCallReason));
            }

            @Test
            public void orderIsRegisteredBeforeSubscriptionToEventGateway() {
                requestObserver.assertValueCount(1);

                verify(orderEventGatewayMock).observable();
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

            public class OnPatialCloseEvent {

                private OrderEvent partialCloseEvent;

                @Before
                public void setUp() {
                    partialCloseEvent = sendOrderEvent(orderForTest, OrderEventType.PARTIAL_CLOSE_OK);
                }

                @Test
                public void submitEventReceived() {
                    testObserver.assertValue(partialCloseEvent);
                }

                @Test
                public void observableNotYetCompleted() {
                    testObserver.assertNotComplete();
                }

                public class OnCloseEvent {

                    private OrderEvent closeEvent;

                    @Before
                    public void setUp() {
                        closeEvent = sendOrderEvent(orderForTest, OrderEventType.CLOSE_OK);
                    }

                    @Test
                    public void submitEventReceived() {
                        testObserver.assertValues(partialCloseEvent, closeEvent);
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
