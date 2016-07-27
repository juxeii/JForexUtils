package com.jforex.programming.order.command.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.OrderChangeCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetOpenPriceCommandTest extends CommonCommandForTest {

    private final double newOpenPrice = 0.12;

    @Before
    public void setUp() {
        command = new SetOpenPriceCommand(orderUnderTest, newOpenPrice);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderUnderTest).setOpenPrice(newOpenPrice);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeOpenPriceData);
    }

    @Test
    public void filterIsFalseWhenOpenPriceAlreadySet() {
        orderUnderTest.setOpenPrice(newOpenPrice);

        assertFalse(((OrderChangeCommand<?>) command).filter(orderUnderTest));
    }

    @Test
    public void filterIsTrueWhenNewOpenPriceDiffers() {
        orderUnderTest.setOpenPrice(newOpenPrice + 0.1);

        assertTrue(((OrderChangeCommand<?>) command).filter(orderUnderTest));
    }
}
