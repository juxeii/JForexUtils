package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetAmountCommandTest extends CommonCommandForTest {

    private final double newAmount = 0.12;

    @Before
    public void setUp() {
        command = new SetAmountCommand(orderForTest, newAmount);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setRequestedAmount(newAmount);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeAmountData);
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
