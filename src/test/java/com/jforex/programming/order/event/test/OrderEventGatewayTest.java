package com.jforex.programming.order.event.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventMapper;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IMessageForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IMessage;

import rx.observers.TestSubscriber;

public class OrderEventGatewayTest extends CommonUtilForTest {

    private OrderEventGateway orderEventGateway;

    @Mock
    private OrderEventMapper orderEventMapperMock;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
    private final IMessage message = new IMessageForTest(orderUnderTest,
                                                         IMessage.Type.ORDER_CHANGED_REJECTED,
                                                         Sets.newHashSet());
    private final OrderMessageData orderMessageData = new OrderMessageData(message);

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderEventGateway = new OrderEventGateway(orderEventMapperMock);
    }

    @Test
    public void registerCallRequestIsRoutedToEventMapper() {
        final OrderCallRequest orderCallRequest =
                new OrderCallRequest(orderUnderTest, OrderCallReason.CHANGE_GTT);

        orderEventGateway.registerOrderCallRequest(orderCallRequest);

        verify(orderEventMapperMock).registerOrderCallRequest(orderCallRequest);
    }

    @Test
    public void subscriberIsNotifiedOnOrderMessageData() {
        when(orderEventMapperMock.get(orderMessageData))
                .thenReturn(OrderEventType.CHANGED_REJECTED);

        orderEventGateway.observable().subscribe(subscriber);
        orderEventGateway.onOrderMessageData(orderMessageData);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final OrderEvent orderEvent = subscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(orderUnderTest));
        assertThat(orderEvent.type(), equalTo(OrderEventType.CHANGED_REJECTED));
    }
}
