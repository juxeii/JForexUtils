package com.jforex.programming.order.event.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IMessage;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventMapper;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IMessageForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class OrderEventGatewayTest extends CommonUtilForTest {

    private OrderEventGateway orderEventGateway;

    @Mock
    private OrderEventMapper orderEventMapperMock;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
    private final Subject<IMessage, IMessage> messageSubject = PublishSubject.create();
    private final IMessage message = new IMessageForTest(orderUnderTest,
                                                         IMessage.Type.ORDER_CHANGED_REJECTED,
                                                         Sets.newHashSet());

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderEventGateway = new OrderEventGateway(messageSubject, orderEventMapperMock);
    }

    @Test
    public void registerCallRequestIsRoutedToEventMapper() {
        final OrderCallRequest orderCallRequest =
                new OrderCallRequest(orderUnderTest, OrderCallReason.CHANGE_GTT);

        orderEventGateway.registerOrderCallRequest(orderCallRequest);

        verify(orderEventMapperMock).registerOrderCallRequest(orderCallRequest);
    }

    @Test
    public void subscriberIsNotNotifiedWhenNotAnOrderRelatedMessage() {
        final IMessage calendarMessage = new IMessageForTest(null,
                                                             IMessage.Type.CALENDAR,
                                                             Sets.newHashSet());

        orderEventGateway.observable().subscribe(subscriber);
        messageSubject.onNext(calendarMessage);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(0);
    }

    @Test
    public void subscriberIsNotifiedOnOrderMessageData() {
        when(orderEventMapperMock.get(any()))
                .thenReturn(OrderEventType.CHANGED_REJECTED);

        orderEventGateway.observable().subscribe(subscriber);
        messageSubject.onNext(message);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final OrderEvent orderEvent = subscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(orderUnderTest));
        assertThat(orderEvent.type(), equalTo(OrderEventType.CHANGED_REJECTED));
    }
}
