package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SetAmountCommandData;

public class SetAmountCommandDataTest extends CommonCommandForTest {

    private final double newAmount = 0.12;

    @Before
    public void setUp() {
        commandData = new SetAmountCommandData(orderForTest, newAmount);
    }

    @Test
    public void orderEventTypesAreCorrect() {
        assertDoneOrderEventTypes(CHANGED_AMOUNT);

        assertRejectOrderEventTypes(CHANGE_AMOUNT_REJECTED);

        assertAllOrderEventTypes(NOTIFICATION,
                                 CHANGED_AMOUNT,
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
