package com.jforex.programming.order.event.test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IMessage;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderCallResult;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventConsumer;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderMessageData;
import com.jforex.programming.order.event.OrderEventTypeEvaluator;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderEventGatewayTest extends CommonUtilForTest {

    private OrderEventGateway orderGateway;

    @Mock private OrderEventTypeEvaluator orderMessageDataToEventMock;
    @Mock private OrderEventConsumer orderEventConsumerMockA;
    @Mock private OrderEventConsumer orderEventConsumerMockB;
    @Captor private ArgumentCaptor<OrderEvent> orderEventCaptor;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final IMessage message = someOrderMessage(orderUnderTest);
    private final OrderMessageData orderMessageData = new OrderMessageData(message);
    private final Optional<OrderCallRequest> orderCallRequestOpt = Optional.of(OrderCallRequest.CHANGE_AMOUNT);
    private final Optional<OrderCallRequest> orderCallRequestEmptyOpt = Optional.empty();
    private final OrderEventType orderEvent = OrderEventType.AMOUNT_CHANGE_OK;
    private OrderCallResult orderCallResult;
    // private final CompletableFuture<Void> finishFuture = new
    // CompletableFuture<Void>();

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();
        orderCallResult = new OrderCallResult(Optional.of(orderUnderTest),
                                              Optional.empty(),
                                              orderCallRequestOpt.get());

        orderGateway = new OrderEventGateway(orderMessageDataToEventMock);
    }

    private void setUpMocks() {
        when(orderMessageDataToEventMock.get(orderMessageData, orderCallRequestOpt)).thenReturn(orderEvent);
        when(orderMessageDataToEventMock.get(orderMessageData, orderCallRequestEmptyOpt)).thenReturn(orderEvent);
    }

//    private void verifyOrderEventByConsumer(final OrderEventConsumer consumer) {
//        verify(consumer).onOrderEvent(orderEventCaptor.capture());
//        final OrderEvent orderEvent = orderEventCaptor.getValue();
//        assertThat(orderEvent.order(), equalTo(orderMessageData.order()));
//        assertThat(orderEvent.type(), equalTo(OrderEventType.AMOUNT_CHANGE_OK));
//    }

//    @Test
//    public void testOnOrderMessageDataNotifiesConsumer() {
//        orderGateway.onOrderMessageData(orderMessageData);
//
//        verifyOrderEventByConsumer(orderEventConsumerMockA);
//        verifyOrderEventByConsumer(orderEventConsumerMockB);
//    }

    @Test
    public void testOnOrderMessageInvokesDataToEventMockWithEmptyCallRequest() {
        orderGateway.onOrderMessageData(orderMessageData);

        verify(orderMessageDataToEventMock).get(orderMessageData, orderCallRequestEmptyOpt);
    }

    public class AfterFirstCallResultRegistering {

        @Before
        public void setUp() {
            orderGateway.onOrderCallResult(orderCallResult);
        }

        public class AfterOnMessage {

            @Before
            public void setUp() {
                orderGateway.onOrderMessageData(orderMessageData);
            }

//            @Test
//            public void testOnOrderMessageDataNotifiesConsumer() {
//                verifyOrderEventByConsumer(orderEventConsumerMockA);
//                verifyOrderEventByConsumer(orderEventConsumerMockB);
//            }

            @Test
            public void testOnOrderMessageInvokesDataToEventMockWithFilledCallRequest() {
                verify(orderMessageDataToEventMock).get(orderMessageData, orderCallRequestOpt);
            }

            @Test
            public void testNextOnOrderMessageInvokesDataToEventMockWithEmptyCallRequest() {
                orderGateway.onOrderMessageData(orderMessageData);

                verify(orderMessageDataToEventMock).get(orderMessageData, orderCallRequestEmptyOpt);
            }
        }

        public class AfterSecondCallResultRegistering {

            @Before
            public void setUp() {
                orderGateway.onOrderCallResult(orderCallResult);
            }

            @Test
            public void testOnOrderMessageInvokesDataToEventMockWithFilledCallRequest() {
                orderGateway.onOrderMessageData(orderMessageData);

                verify(orderMessageDataToEventMock).get(orderMessageData, orderCallRequestOpt);
            }

            public class AfterOnMessage {

                @Before
                public void setUp() {
                    orderGateway.onOrderMessageData(orderMessageData);
                }

                public class AfterSecondOnMessage {

                    @Before
                    public void setUp() {
                        orderGateway.onOrderMessageData(orderMessageData);
                    }

                    @Test
                    public void testNextOnOrderMessageInvokesDataToEventMockWithEmptyCallRequest() {
                        orderGateway.onOrderMessageData(orderMessageData);

                        verify(orderMessageDataToEventMock).get(orderMessageData, orderCallRequestEmptyOpt);
                    }
                }
            }
        }
    }
}
