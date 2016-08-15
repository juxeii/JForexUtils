package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.CommandData;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class OrderCallCommandTest extends CommonCommandForTest {

    private OrderCallCommand orderCallCommand;

    @Mock
    private CommandData commandData;
    private final IOrder orderForTest = orderUtilForTest.buyOrderEURUSD();
    private final OrderEventTypeData orderEventTypeData =
            new OrderEventTypeData(EnumSet.of(CLOSE_OK),
                                   EnumSet.of(CLOSE_REJECTED),
                                   EnumSet.of(NOTIFICATION, PARTIAL_CLOSE_OK));

    @Before
    public void setUp() {
        when(commandData.orderEventTypeData()).thenReturn(orderEventTypeData);

        orderCallCommand = new OrderCallCommand(commandData);
    }

    private OrderEvent createEvent(final OrderEventType eventType) {
        return new OrderEvent(orderForTest, eventType);
    }

    @Test
    public void returnedCallableIsCorrect() {
        final Callable<IOrder> callableMock = () -> orderForTest;

        when(commandData.callable()).thenReturn(callableMock);

        assertThat(orderCallCommand.callable(), equalTo(callableMock));
    }

    @Test
    public void returnedCallReasonIsCorrect() {
        final OrderCallReason callableReason = OrderCallReason.CHANGE_AMOUNT;

        when(commandData.callReason()).thenReturn(callableReason);

        assertThat(orderCallCommand.callReason(), equalTo(callableReason));
    }

    @Test
    public void isEventForCommand() {
        assertTrue(orderCallCommand.isEventForCommand(createEvent(CLOSE_OK)));
        assertTrue(orderCallCommand.isEventForCommand(createEvent(CLOSE_REJECTED)));
        assertTrue(orderCallCommand.isEventForCommand(createEvent(NOTIFICATION)));
        assertTrue(orderCallCommand.isEventForCommand(createEvent(PARTIAL_CLOSE_OK)));
    }

    @Test
    public void isDoneEvent() {
        assertTrue(orderCallCommand.isDoneEvent(createEvent(CLOSE_OK)));
        assertFalse(orderCallCommand.isDoneEvent(createEvent(CLOSE_REJECTED)));
        assertFalse(orderCallCommand.isDoneEvent(createEvent(NOTIFICATION)));
        assertFalse(orderCallCommand.isDoneEvent(createEvent(PARTIAL_CLOSE_OK)));
    }

    @Test
    public void isRejectEvent() {
        assertTrue(orderCallCommand.isRejectEvent(createEvent(CLOSE_REJECTED)));
        assertFalse(orderCallCommand.isRejectEvent(createEvent(CLOSE_OK)));
        assertFalse(orderCallCommand.isRejectEvent(createEvent(NOTIFICATION)));
        assertFalse(orderCallCommand.isRejectEvent(createEvent(PARTIAL_CLOSE_OK)));
    }
}
