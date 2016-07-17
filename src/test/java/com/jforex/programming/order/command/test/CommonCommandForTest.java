package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;

import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;

public class CommonCommandForTest extends CommonUtilForTest {

    protected OrderCallCommand command;

    protected final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();

    public void setUpCommon() {
        initCommonTestFramework();
    }

    protected void assertCallableOrder() throws Exception {
        final Callable<IOrder> callable = command.callable();
        final IOrder order = callable.call();

        assertThat(order, equalTo(orderUnderTest));
    }

    protected void assertOrderEventTypeData(final OrderEventTypeData orderEventTypeData) {
        assertThat(command.orderEventTypeData(), equalTo(orderEventTypeData));
    }
}
