package com.jforex.programming.order.event.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.IMessage;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderCallResult;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IMessageForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class OrderEventGatewayTest extends CommonUtilForTest {

    private OrderEventGateway orderGateway;

    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private Observable<OrderEvent> orderEventObservable;
    private final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
    private IMessage message;
    private OrderMessageData orderMessageData;
    private final Optional<OrderCallRequest> orderCallRequestOpt = Optional.of(OrderCallRequest.CHANGE_SL);
    private OrderCallResult orderCallResult;

    @Before
    public void setUp() {
        initCommonTestFramework();
        message = new IMessageForTest(orderUnderTest,
                                      IMessage.Type.ORDER_CHANGED_REJECTED,
                                      createSet());
        orderMessageData = new OrderMessageData(message);
        orderCallResult = new OrderCallResult(Optional.of(orderUnderTest),
                                              Optional.empty(),
                                              orderCallRequestOpt.get());

        orderGateway = new OrderEventGateway();
        orderEventObservable = orderGateway.observable();
        orderEventObservable.subscribe(subscriber);
    }

    @Test
    public void testSubscriberIsNotifiedWithourRefiningSinceNoCallResultRegistered() {
        orderGateway.onOrderMessageData(orderMessageData);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final OrderEvent orderEvent = subscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(orderUnderTest));
        assertThat(orderEvent.type(), equalTo(OrderEventType.CHANGE_REJECTED));
    }

    public class AfterCallResultRegistering {

        @Before
        public void setUp() {
            orderGateway.onOrderCallResult(orderCallResult);
            orderGateway.onOrderMessageData(orderMessageData);
        }

        @Test
        public void testSubscriberIsNotifiedForARefinedRejectEvent() {
            subscriber.assertNoErrors();
            subscriber.assertValueCount(1);
            final OrderEvent orderEvent = subscriber.getOnNextEvents().get(0);

            assertThat(orderEvent.order(), equalTo(orderUnderTest));
            assertThat(orderEvent.type(), equalTo(OrderEventType.CHANGE_SL_REJECTED));
        }
    }
}
