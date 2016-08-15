package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SetGTTCommand;

public class SetGTTCommandTest extends CommonCommandForTest {

    private final long newGTT = 1234L;

    @Before
    public void setUp() {
        command = new SetGTTCommand(orderForTest, newGTT);
    }

    @Test
    public void orderEventTestAreCorrect() {
        assertIsDoneEvent(CHANGED_GTT);

        assertIsRejectEvent(CHANGE_GTT_REJECTED);

        assertEventIsForCommand(CHANGED_GTT,
                                CHANGE_GTT_REJECTED);
    }

    @Test
    public void orderCallReasonIsCorrect() {
        assertCallReason(OrderCallReason.CHANGE_GTT);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setGoodTillTime(newGTT);
    }

    @Test
    public void filterIsFalseWhenNewGTTAlreadySet() {
        orderUtilForTest.setGTT(orderForTest, newGTT);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenNewGTTDiffers() {
        orderUtilForTest.setGTT(orderForTest, newGTT + 1L);

        assertFilterIsSet();
    }
}
