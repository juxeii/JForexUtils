package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SetOpenPriceCommand;

public class SetOpenPriceCommandTest extends CommonCommandForTest {

    private final double newOpenPrice = 0.12;

    @Before
    public void setUp() {
        command = new SetOpenPriceCommand(orderForTest, newOpenPrice);
    }

    @Test
    public void orderEventTestAreCorrect() {
        assertIsDoneEvent(CHANGED_PRICE);

        assertIsRejectEvent(CHANGE_PRICE_REJECTED);

        assertEventIsForCommand(CHANGED_PRICE,
                                CHANGE_PRICE_REJECTED);
    }

    @Test
    public void orderCallReasonIsCorrect() {
        assertCallReason(OrderCallReason.CHANGE_PRICE);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setOpenPrice(newOpenPrice);
    }

    @Test
    public void filterIsFalseWhenOpenPriceAlreadySet() {
        orderUtilForTest.setOpenPrice(orderForTest, newOpenPrice);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenNewOpenPriceDiffers() {
        orderUtilForTest.setOpenPrice(orderForTest, newOpenPrice + 0.1);

        assertFilterIsSet();
    }
}
