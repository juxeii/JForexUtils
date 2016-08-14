package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.OrderChangeCommand;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.common.CommonUtilForTest;

public class CommonCommandForTest extends CommonUtilForTest {

    protected OrderCallCommand command;

    protected final IOrder orderForTest = buyOrderEURUSD;

    protected void assertCallableOrder() throws Exception {
        final Callable<IOrder> callable = command.callable();
        final IOrder order = callable.call();

        assertThat(order, equalTo(orderForTest));
    }

    protected void assertFilterNotSet() {
        assertFalse(((OrderChangeCommand<?>) command).filter());
    }

    protected void assertFilterIsSet() {
        assertTrue(((OrderChangeCommand<?>) command).filter());
    }

    protected void assertEventTypeData(final OrderEventTypeData orderEventTypeData) {
        assertThat(command.orderEventTypeData(), equalTo(orderEventTypeData));
    }

    protected void assertCallReason(final OrderCallReason orderCallReason) {
        assertThat(command.callReason(), equalTo(orderCallReason));
    }
}
