package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.OrderChangeCommand;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

public class CommonCommandForTest extends CommonUtilForTest {

    protected OrderCallCommand command;

    protected final IOrderForTest orderForTest = IOrderForTest.buyOrderEURUSD();

    protected void assertCallableOrder() throws Exception {
        final Callable<IOrder> callable = command.callable();
        final IOrder order = callable.call();

        assertThat(order, equalTo(orderForTest));
    }

    protected void assertOrderEventTypeData(final OrderEventTypeData orderEventTypeData) {
        assertThat(command.orderEventTypeData(), equalTo(orderEventTypeData));
    }

    protected void assertFilterNotSet() {
        assertFalse(((OrderChangeCommand<?>) command).filter());
    }

    protected void assertFilterIsSet() {
        assertTrue(((OrderChangeCommand<?>) command).filter());
    }
}
