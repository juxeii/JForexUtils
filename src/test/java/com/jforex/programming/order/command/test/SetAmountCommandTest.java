package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SetAmountCommand;

public class SetAmountCommandTest extends CommonCommandForTest {

    private final double newAmount = 0.12;

    @Before
    public void setUp() {
        command = new SetAmountCommand(orderForTest, newAmount);
    }

    @Test
    public void orderEventTestAreCorrect() {
        assertIsDoneEvent(CHANGED_AMOUNT);

        assertIsRejectEvent(CHANGE_AMOUNT_REJECTED);

        assertEventIsForCommand(CHANGED_AMOUNT,
                                CHANGE_AMOUNT_REJECTED);
    }

    @Test
    public void orderCallReasonIsCorrect() {
        assertCallReason(OrderCallReason.CHANGE_AMOUNT);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setRequestedAmount(newAmount);
    }

    @Test
    public void filterIsFalseWhenNewAmountAlreadySet() {
        orderUtilForTest.setRequestedAmount(orderForTest, newAmount);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenNewAmountDiffers() {
        orderUtilForTest.setRequestedAmount(orderForTest, newAmount + 0.1);

        assertFilterIsSet();
    }
}
