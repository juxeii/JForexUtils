package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SetTPCommand;

public class SetTPCommandTest extends CommonCommandForTest {

    private final double newTP = 1.2345;

    @Before
    public void setUp() {
        command = new SetTPCommand(orderForTest, newTP);
    }

    @Test
    public void orderEventTestAreCorrect() {
        assertIsDoneEvent(CHANGED_TP);

        assertIsRejectEvent(CHANGE_TP_REJECTED);

        assertEventIsForCommand(CHANGED_TP,
                                CHANGE_TP_REJECTED);
    }

    @Test
    public void orderCallReasonIsCorrect() {
        assertCallReason(OrderCallReason.CHANGE_TP);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setTakeProfitPrice(newTP);
    }

    @Test
    public void filterIsFalseWhenNewTPAlreadySet() {
        orderUtilForTest.setTP(orderForTest, newTP);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenNewTPDiffers() {
        orderUtilForTest.setTP(orderForTest, newTP + 0.1);

        assertFilterIsSet();
    }
}
