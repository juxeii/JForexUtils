package com.jforex.programming.order.event.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class OrderEventTest extends QuoteProviderForTest {

    private final OrderEvent orderEvent = submitEvent;

    @Test
    public void allAccessorsAreCorrect() {
        assertThat(orderEvent.order(), equalTo(buyOrderEURUSD));
        assertThat(orderEvent.type(), equalTo(OrderEventType.SUBMIT_OK));
        assertTrue(orderEvent.isInternal());
    }

    @Test
    public void isEqualsContractOK() {
        testEqualsContract(orderEvent);
    }
}