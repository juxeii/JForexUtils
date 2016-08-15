package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SetSLCommand;

public class SetSLCommandTest extends CommonCommandForTest {

    private final double newSL = 1.2345;

    @Before
    public void setUp() {
        command = new SetSLCommand(orderForTest, newSL);
    }

    @Test
    public void orderEventTestAreCorrect() {
        assertIsDoneEvent(CHANGED_SL);

        assertIsRejectEvent(CHANGE_SL_REJECTED);

        assertEventIsForCommand(CHANGED_SL,
                                CHANGE_SL_REJECTED);
    }

    @Test
    public void orderCallReasonIsCorrect() {
        assertCallReason(OrderCallReason.CHANGE_SL);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setStopLossPrice(newSL);
    }

    @Test
    public void filterIsFalseWhenNewSLAlreadySet() {
        orderUtilForTest.setSL(orderForTest, newSL);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenNewSLDiffers() {
        orderUtilForTest.setSL(orderForTest, newSL + 0.1);

        assertFilterIsSet();
    }
}
