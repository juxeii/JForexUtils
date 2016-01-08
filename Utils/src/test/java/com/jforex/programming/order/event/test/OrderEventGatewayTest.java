package com.jforex.programming.order.event.test;

import static org.junit.Assert.assertTrue;

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
import com.jforex.programming.order.event.OrderMessageData;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderEventGatewayTest extends CommonUtilForTest {

    private OrderEventGateway orderGateway;

    @Mock private OrderEventConsumer orderEventConsumerMockA;
    @Mock private OrderEventConsumer orderEventConsumerMockB;
    @Captor private ArgumentCaptor<OrderEvent> orderEventCaptor;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final IMessage message = someOrderMessage(orderUnderTest);
    private final OrderMessageData orderMessageData = new OrderMessageData(message);
    private final Optional<OrderCallRequest> orderCallRequestOpt = Optional.of(OrderCallRequest.CHANGE_AMOUNT);
    // private final Optional<OrderCallRequest> orderCallRequestEmptyOpt =
    // Optional.empty();
    // private final OrderEventType orderEvent =
    // OrderEventType.AMOUNT_CHANGE_OK;
    private OrderCallResult orderCallResult;

    @Before
    public void setUp() {
        initCommonTestFramework();
        orderCallResult = new OrderCallResult(Optional.of(orderUnderTest),
                                              Optional.empty(),
                                              orderCallRequestOpt.get());

        orderGateway = new OrderEventGateway();
    }

    public class AfterFirstCallResultRegistering {

        @Before
        public void setUp() {
            orderGateway.onOrderCallResult(orderCallResult);
        }

        @Test
        public void testDummy() {
            assertTrue(true);
        }

        public class AfterOnMessage {

            @Before
            public void setUp() {
                orderGateway.onOrderMessageData(orderMessageData);
            }

            @Test
            public void testDummy() {
                assertTrue(true);
            }
        }

        public class AfterSecondCallResultRegistering {

            @Before
            public void setUp() {
                orderGateway.onOrderCallResult(orderCallResult);
            }

            @Test
            public void testDummy() {
                assertTrue(true);
            }

            public class AfterOnMessage {

                @Before
                public void setUp() {
                    orderGateway.onOrderMessageData(orderMessageData);
                }

                @Test
                public void testDummy() {
                    assertTrue(true);
                }

                public class AfterSecondOnMessage {

                    @Before
                    public void setUp() {
                        orderGateway.onOrderMessageData(orderMessageData);
                    }

                    @Test
                    public void testDummy() {
                        assertTrue(true);
                    }
                }
            }
        }
    }
}
