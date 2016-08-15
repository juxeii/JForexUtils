package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.CommandData;
import com.jforex.programming.order.command.OrderChangeCommandData;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

public class CommonCommandForTest extends CommonUtilForTest {

    protected CommandData commandData;

    protected final IOrder orderForTest = buyOrderEURUSD;

    protected void assertCallableOrder() throws Exception {
        final Callable<IOrder> callable = commandData.callable();
        final IOrder order = callable.call();

        assertThat(order, equalTo(orderForTest));
    }

    protected void assertFilterNotSet() {
        assertFalse(((OrderChangeCommandData<?>) commandData).isValueNotSet());
    }

    protected void assertFilterIsSet() {
        assertTrue(((OrderChangeCommandData<?>) commandData).isValueNotSet());
    }

    protected void assertCallReason(final OrderCallReason orderCallReason) {
        assertThat(commandData.callReason(), equalTo(orderCallReason));
    }

    protected void assertAllOrderEventTypes(final OrderEventType... orderEventTypes) {
        assertThat(commandData.orderEventTypeData().allEventTypes(), containsInAnyOrder(orderEventTypes));
    }

    protected void assertDoneOrderEventTypes(final OrderEventType... orderEventTypes) {
        assertThat(commandData.orderEventTypeData().doneEventTypes(), containsInAnyOrder(orderEventTypes));
    }

    protected void assertRejectOrderEventTypes(final OrderEventType... orderEventTypes) {
        assertThat(commandData.orderEventTypeData().rejectEventTypes(), containsInAnyOrder(orderEventTypes));
    }
}
