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

import rx.Observable;
import rx.observers.TestSubscriber;

public class OrderEventGatewayTest extends CommonUtilForTest {

    private OrderEventGateway orderGateway;

    @Mock
    private OrderEventMapper orderEventMapperMock;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private Observable<OrderEvent> orderEventObservable;
    private final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
    private IMessage message;
    private OrderMessageData orderMessageData;

    @Before
    public void setUp() {
        initCommonTestFramework();

        message = new IMessageForTest(orderUnderTest,
                                      IMessage.Type.ORDER_CHANGED_REJECTED,
                                      Sets.newHashSet());
        orderMessageData = new OrderMessageData(message);

        orderGateway = new OrderEventGateway(orderEventMapperMock);
        orderEventObservable = orderGateway.observable();
        orderEventObservable.subscribe(subscriber);
    }

    @Test
    public void testSubscriberIsNotifiedWithoutRefiningSinceNoCallResultRegistered() {
        when(orderEventMapperMock.get(any())).thenReturn(OrderEventType.CHANGED_REJECTED);

        orderGateway.onOrderMessageData(orderMessageData);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final OrderEvent orderEvent = subscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(orderUnderTest));
        assertThat(orderEvent.type(), equalTo(OrderEventType.CHANGED_REJECTED));
    }

    @Test
    public void testSubscriberIsNotifiedForARefinedRejectEvent() {
        when(orderEventMapperMock.get(any())).thenReturn(OrderEventType.CHANGE_SL_REJECTED);

        orderGateway
                .registerOrderCallRequest(new OrderCallRequest(orderUnderTest, OrderCallReason.CHANGE_SL));
        orderGateway.onOrderMessageData(orderMessageData);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final OrderEvent orderEvent = subscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(orderUnderTest));
        assertThat(orderEvent.type(), equalTo(OrderEventType.CHANGE_SL_REJECTED));
    }
}
