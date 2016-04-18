package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IMessage;
import com.jforex.programming.order.OrderChange;
import com.jforex.programming.order.OrderCreate;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventConsumer;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IMessageForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import rx.Observable;
import rx.observers.TestSubscriber;

public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock private OrderCreate orderCreateMock;
    @Mock private OrderChange orderChangeMock;
    @Mock private OrderEventConsumer orderEventConsumerMock;
    @Captor private ArgumentCaptor<OrderEvent> orderEventCaptor;
    private OrderEventGateway orderEventGateway;
    private Observable<OrderEvent> orderEventObservable;
    private final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private IMessage message;
    private OrderMessageData orderMessageData;
    private final Map<OrderEventType, OrderEventConsumer> orderEventConsumerMap = new HashMap<>();

    @Before
    public void setUp() {
        initCommonTestFramework();

        message = new IMessageForTest(orderUnderTest,
                                      IMessage.Type.ORDER_CHANGED_REJECTED,
                                      createSet());
        orderMessageData = new OrderMessageData(message);
        orderEventGateway = new OrderEventGateway();
        orderEventObservable = orderEventGateway.observable();
        orderEventObservable.subscribe(subscriber);
        orderUtil = new OrderUtil(orderCreateMock,
                                  orderChangeMock,
                                  orderEventGateway);
    }

    @Test
    public void testRegisterEventConsumerIsNotifyedOnEvent() {
        orderUtil.registerEventConsumer(orderUnderTest, orderEventConsumerMock);
        orderEventGateway.onOrderMessageData(orderMessageData);

        verify(orderEventConsumerMock).onOrderEvent(orderEventCaptor.capture());
        final OrderEvent orderEvent = orderEventCaptor.getValue();
        assertThat(orderEvent.order(), equalTo(orderUnderTest));
        assertThat(orderEvent.type(), equalTo(OrderEventType.CHANGE_REJECTED));
    }

    @Test
    public void testRegisterEventConsumerMapIsNotifyedOnEvent() {
        orderEventConsumerMap.put(OrderEventType.CHANGE_REJECTED, orderEventConsumerMock);

        orderUtil.registerEventConsumerMap(orderUnderTest, orderEventConsumerMap);
        orderEventGateway.onOrderMessageData(orderMessageData);

        verify(orderEventConsumerMock).onOrderEvent(orderEventCaptor.capture());
        final OrderEvent orderEvent = orderEventCaptor.getValue();
        assertThat(orderEvent.order(), equalTo(orderUnderTest));
        assertThat(orderEvent.type(), equalTo(OrderEventType.CHANGE_REJECTED));
    }
}
