package com.jforex.programming.order.event.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

public class OrderMessageDataTest extends CommonUtilForTest {

    private OrderMessageData orderMessageData;

    private final IOrderForTest testOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrder.State orderState = IOrder.State.FILLED;
    private final IMessage.Type messageType = IMessage.Type.ORDER_CHANGED_OK;
    private final Set<IMessage.Reason> messageReasons = Sets.newHashSet();
    private final IMessage message = mockForIMessage(testOrder,
                                                     messageType,
                                                     messageReasons);

    @Before
    public void setUp() {
        testOrder.setState(IOrder.State.FILLED);

        orderMessageData = new OrderMessageData(message);
    }

    @Test
    public void testOrderReturnsCorrectOrderInstance() {
        assertThat(orderMessageData.order(), equalTo(testOrder));
    }

    @Test
    public void testOrderStateReturnsCorrectState() {
        assertThat(orderMessageData.orderState(), equalTo(orderState));
    }

    @Test
    public void testMessageTypeReturnsCorrectType() {
        assertThat(orderMessageData.messageType(), equalTo(messageType));
    }

    @Test
    public void testMessageReasonsReturnsCorrectReasons() {
        assertThat(orderMessageData.messageReasons(), equalTo(messageReasons));
    }
}
