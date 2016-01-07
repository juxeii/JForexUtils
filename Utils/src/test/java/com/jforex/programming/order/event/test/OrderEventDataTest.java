package com.jforex.programming.order.event.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.event.OrderMessageData;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IMessageForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;

public class OrderEventDataTest extends CommonUtilForTest {

    private OrderMessageData orderEventDataByDedicatedParams;
    private OrderMessageData orderEventDataByMessage;

    private IOrderForTest testOrder;
    private IMessageForTest message;
    private final IOrder.State orderState = IOrder.State.FILLED;
    private final IMessage.Type messageType = IMessage.Type.ORDER_CHANGED_OK;
    private final Set<IMessage.Reason> messageReasons = Collections.emptySet();

    @Before
    public void setUp() {
        initCommonTestFramework();
        testOrder = IOrderForTest.buyOrderEURUSD();
        testOrder.setState(IOrder.State.FILLED);
        message = new IMessageForTest(testOrder, messageType, messageReasons);

        orderEventDataByDedicatedParams = new OrderMessageData(message);
        orderEventDataByMessage = new OrderMessageData(message);
    }

    @Test
    public void testOrderReturnsCorrectOrderInstance() {
        assertThat(orderEventDataByDedicatedParams.order(), equalTo(testOrder));
        assertThat(orderEventDataByMessage.order(), equalTo(testOrder));
    }

    @Test
    public void testOrderStateReturnsCorrectState() {
        assertThat(orderEventDataByDedicatedParams.orderState(), equalTo(orderState));
        assertThat(orderEventDataByMessage.orderState(), equalTo(orderState));
    }

    @Test
    public void testMessageTypeReturnsCorrectType() {
        assertThat(orderEventDataByDedicatedParams.messageType(), equalTo(messageType));
        assertThat(orderEventDataByMessage.messageType(), equalTo(messageType));
    }

    @Test
    public void testMessageReasonsReturnsCorrectReasons() {
        assertThat(orderEventDataByDedicatedParams.messageReasons(), equalTo(messageReasons));
        assertThat(orderEventDataByMessage.messageReasons(), equalTo(messageReasons));
    }
}
