package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetAmountCommandTest extends CommonCommandForTest {

    private final double newAmount = 0.12;

    @Before
    public void setUp() {
        command = new SetAmountCommand(orderUnderTest, newAmount);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderUnderTest).setRequestedAmount(newAmount);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeAmountData);
    }
}
