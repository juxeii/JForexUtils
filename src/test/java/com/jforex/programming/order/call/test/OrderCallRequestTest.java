package com.jforex.programming.order.call.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class OrderCallRequestTest extends QuoteProviderForTest {

    private final OrderCallRequest orderCallRequest = new OrderCallRequest(buyOrderEURUSD, OrderCallReason.SUBMIT);

    @Test
    public void allAccessorsAreCorrect() {
        assertThat(orderCallRequest.order(), equalTo(buyOrderEURUSD));
        assertThat(orderCallRequest.reason(), equalTo(OrderCallReason.SUBMIT));
    }

    @Test
    public void isEqualsContractOK() {
        testEqualsContract(orderCallRequest);
    }
}
