package com.jforex.programming.order.call.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderCallResult;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

public class OrderCallResultTest extends CommonUtilForTest {

    private OrderCallResult callResultNoException;
    private OrderCallResult callResultWithException;

    private final IOrderForTest order = IOrderForTest.buyOrderEURUSD();
    private final OrderCallRequest orderCallRequest = OrderCallRequest.CHANGE_AMOUNT;

    @Before
    public void setUp() {
        callResultNoException = new OrderCallResult(Optional.of(order),
                                                    emptyJFExceptionOpt,
                                                    orderCallRequest);
        callResultWithException = new OrderCallResult(Optional.of(order),
                                                      jfExceptionOpt,
                                                      orderCallRequest);
    }

    @Test
    public void testContentAreCorrectForNoExcpetion() {
        assertThat(callResultNoException.orderOpt().get(), equalTo(order));
        assertFalse(callResultNoException.exceptionOpt().isPresent());
        assertThat(callResultNoException.callRequest(), equalTo(orderCallRequest));
    }

    @Test
    public void testContentAreCorrectWithExcpetion() {
        assertThat(callResultWithException.orderOpt().get(), equalTo(order));
        assertThat(callResultWithException.exceptionOpt().get(), equalTo(jfExceptionOpt.get()));
        assertThat(callResultWithException.callRequest(), equalTo(orderCallRequest));
    }
}
