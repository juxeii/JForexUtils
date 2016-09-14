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
import com.jforex.programming.order.command.CommandData;
import com.jforex.programming.order.command.Command;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import io.reactivex.functions.Action;

public class CommandTester extends CommonUtilForTest {

    @Mock
    protected Action completedActionMock;
    @Mock
    protected Consumer<Throwable> errorActionMock;
    @Mock
    protected IEngineUtil iengineUtilMock;
    protected Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;
    protected static final int noOfRetries = 3;
    protected static final long retryDelay = 1500L;

    protected void assertEventTypesForCommand(final EnumSet<OrderEventType> eventTypes,
                                              final Command command) {
        final CommandData data = command.data();
        assertEventTypes(eventTypes, data::isEventTypeForCommand);
    }

    protected void assertFinishEventTypesForCommand(final EnumSet<OrderEventType> eventTypes,
                                                    final Command command) {
        final CommandData data = command.data();
        assertEventTypes(eventTypes, data::isFinishEventType);
    }

    protected void assertRejectEventTypesForCommand(final EnumSet<OrderEventType> eventTypes,
                                                    final Command command) {
        final CommandData data = command.data();
        assertEventTypes(eventTypes, data::isRejectEventType);
    }

    protected void assertEventTypes(final EnumSet<OrderEventType> eventTypes,
                                    final Function<OrderEventType, Boolean> function) {
        eventTypes.forEach(eventType -> assertTrue(function.apply(eventType)));
        final EnumSet<OrderEventType> complement = EnumSet.complementOf(eventTypes);
        complement.forEach(eventType -> assertFalse(function.apply(eventType)));
    }

    protected void assertNoRetryParams(final Command command) {
        final CommandData data = command.data();
        assertThat(data.noOfRetries(), equalTo(0));
        assertThat(data.retryDelayInMillis(), equalTo(0L));
    }

    protected void assertRetryParams(final Command command) {
        final CommandData data = command.data();
        assertThat(data.noOfRetries(), equalTo(noOfRetries));
        assertThat(data.retryDelayInMillis(), equalTo(retryDelay));
    }

    protected void assertActionsNotNull(final Command command) throws Exception {
        final CommandData data = command.data();
        data.startAction().run();
        data.eventAction().accept(new OrderEvent(buyOrderEURUSD,
                                                 OrderEventType.SUBMIT_OK,
                                                 true));
        data.completedAction().run();
        data.errorAction().accept(jfException);
    }
}
