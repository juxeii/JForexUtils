package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.CloseCommand;

public class CloseCommandTest extends CommonCommandForTest {

    @Before
    public void setUp() {
        command = new CloseCommand(orderForTest);
    }

    @Test
    public void orderEventTestAreCorrect() {
        assertIsDoneEvent(CLOSE_OK);

        assertIsRejectEvent(CLOSE_REJECTED);

        assertEventIsForCommand(CLOSE_OK,
                                CLOSE_REJECTED,
                                PARTIAL_CLOSE_OK);
    }

    @Test
    public void orderCallReasonIsCorrect() {
        assertCallReason(OrderCallReason.CLOSE);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).close();
    }

    @Test
    public void filterIsFalseWhenOrderAlreadyClosed() {
        orderUtilForTest.setState(orderForTest, IOrder.State.CLOSED);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenOrderIsActive() {
        orderUtilForTest.setState(orderForTest, IOrder.State.FILLED);

        assertFilterIsSet();
    }
}
