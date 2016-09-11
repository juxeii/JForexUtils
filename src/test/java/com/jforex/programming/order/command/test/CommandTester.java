package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.command.CommonCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.functions.Action0;

public class CommandTester extends CommonUtilForTest {

    @Mock
    protected Action0 completedActionMock;
    @Mock
    protected Consumer<Throwable> errorActionMock;
    @Mock
    protected IEngineUtil iengineUtilMock;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;
    protected static final int noOfRetries = 3;
    protected static final long retryDelay = 1500L;

    protected void assertEventTypesForCommand(final EnumSet<OrderEventType> eventTypes,
                                              final CommonCommand command) {
        assertEventTypes(eventTypes, command::isEventTypeForCommand);
    }

    protected void assertFinishEventTypesForCommand(final EnumSet<OrderEventType> eventTypes,
                                                    final CommonCommand command) {
        assertEventTypes(eventTypes, command::isFinishEventType);
    }

    protected void assertRejectEventTypesForCommand(final EnumSet<OrderEventType> eventTypes,
                                                    final CommonCommand command) {
        assertEventTypes(eventTypes, command::isRejectEventType);
    }

    protected void assertEventTypes(final EnumSet<OrderEventType> eventTypes,
                                    final Function<OrderEventType, Boolean> function) {
        eventTypes.forEach(eventType -> assertTrue(function.apply(eventType)));
        final EnumSet<OrderEventType> complement = EnumSet.complementOf(eventTypes);
        complement.forEach(eventType -> assertFalse(function.apply(eventType)));
    }

    protected void assertNoRetryParams(final CommonCommand command) {
        assertThat(command.noOfRetries(), equalTo(0));
        assertThat(command.retryDelayInMillis(), equalTo(0L));
    }

    protected void assertRetryParams(final CommonCommand command) {
        assertThat(command.noOfRetries(), equalTo(noOfRetries));
        assertThat(command.retryDelayInMillis(), equalTo(retryDelay));
    }

    protected void assertActionsNotNull(final CommonCommand command) {
        command.startAction().call();
        command.eventAction().accept(new OrderEvent(buyOrderEURUSD,
                                                    OrderEventType.SUBMIT_OK,
                                                    true));
        command.completedAction().call();
        command.errorAction().accept(jfException);
    }
}
