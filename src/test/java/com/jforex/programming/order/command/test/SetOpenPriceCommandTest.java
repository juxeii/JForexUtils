package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetOpenPriceCommandTest extends CommonCommandForTest {

    private final double newOpenPrice = 0.12;

    @Before
    public void setUp() {
        command = new SetOpenPriceCommand(orderForTest, newOpenPrice);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setOpenPrice(newOpenPrice);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeOpenPriceData);
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
