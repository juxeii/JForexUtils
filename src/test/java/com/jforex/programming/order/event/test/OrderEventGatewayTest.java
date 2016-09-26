package com.jforex.programming.order.event.test;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventFactory;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.test.common.CommonUtilForTest;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class OrderEventGatewayTest extends CommonUtilForTest {

    private OrderEventGateway orderEventGateway;

    @Mock
    private OrderEventFactory orderEventMapperMock;
    private final IOrder orderUnderTest = buyOrderEURUSD;
    private final TestObserver<OrderEvent> subscriber = TestObserver.create();
    private final Subject<IMessage> messageSubject = PublishSubject.create();
    private final IMessage message = mockForIMessage(orderUnderTest,
                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
                                                     Sets.newHashSet());

    @Before
    public void setUp() {
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
        final IMessage calendarMessage = mockForIMessage(null,
                                                         IMessage.Type.CALENDAR,
                                                         Sets.newHashSet());

        orderEventGateway
            .observable()
            .subscribe(subscriber);
        messageSubject.onNext(calendarMessage);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(0);
    }

    @Test
    public void subscriberIsNotifiedOnOrderMessageData() {
        when(orderEventMapperMock.fromMessage(any()))
            .thenReturn(changedRejectEvent);

        orderEventGateway
            .observable()
            .subscribe(subscriber);
        messageSubject.onNext(message);

        subscriber.assertNoErrors();
        subscriber.assertValue(changedRejectEvent);
    }
}
