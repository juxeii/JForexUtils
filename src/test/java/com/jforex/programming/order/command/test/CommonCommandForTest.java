package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.OrderChangeCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
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
        assertFalse(((OrderChangeCommand<?>) command).isValueNotSet());
    }

    protected void assertFilterIsSet() {
        assertTrue(((OrderChangeCommand<?>) command).isValueNotSet());
    }

    protected void assertCallReason(final OrderCallReason orderCallReason) {
        assertThat(command.callReason(), equalTo(orderCallReason));
    }

    protected void assertEventIsForCommand(final OrderEventType... orderEventTypes) {
        assertEvents(command::isEventForCommand, orderEventTypes);
    }

    protected void assertIsDoneEvent(final OrderEventType... orderEventTypes) {
        assertEvents(command::isDoneEvent, orderEventTypes);
    }

    protected void assertIsRejectEvent(final OrderEventType... orderEventTypes) {
        assertEvents(command::isRejectEvent, orderEventTypes);
    }

    protected void assertEvents(final Function<OrderEvent, Boolean> testFunction,
                                final OrderEventType... orderEventTypes) {
        new ArrayList<>(Arrays.asList(orderEventTypes))
            .forEach(type -> {
                final OrderEvent orderEvent = new OrderEvent(orderForTest, type);
                assertTrue(testFunction.apply(orderEvent));
            });
    }
}
