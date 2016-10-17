package com.jforex.programming.order.event.test;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IMessage;
import com.google.common.collect.Sets;
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
    private TestObserver<OrderEvent> subscriber;
    private final Subject<IMessage> messageSubject = PublishSubject.create();
    private final IMessage message = mockForIMessage(buyOrderEURUSD,
                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
                                                     Sets.newHashSet());

    @Before
    public void setUp() {
        orderEventGateway = new OrderEventGateway(messageSubject, orderEventMapperMock);
    }

    @Test
    public void subscriberIsNotNotifiedWhenNotAnOrderRelatedMessage() {
        final IMessage calendarMessage = mockForIMessage(null,
                                                         IMessage.Type.CALENDAR,
                                                         Sets.newHashSet());

        subscriber = orderEventGateway
            .observable()
            .test();

        messageSubject.onNext(calendarMessage);

        subscriber
            .assertNoErrors()
            .assertValueCount(0);
    }

    @Test
    public void subscriberIsNotifiedOnOrderMessageData() {
        when(orderEventMapperMock.fromMessage(any()))
            .thenReturn(changedRejectEvent);

        subscriber = orderEventGateway
            .observable()
            .test();

        messageSubject.onNext(message);

        subscriber
            .assertNoErrors()
            .assertValue(changedRejectEvent);
    }

    @Test
    public void importOrderEmitsCorrectOrderEvent() {
        subscriber = orderEventGateway
            .observable()
            .test();

        orderEventGateway.importOrder(buyOrderEURUSD);

        subscriber
            .assertNoErrors()
            .assertValue(submitEvent);
    }
}
