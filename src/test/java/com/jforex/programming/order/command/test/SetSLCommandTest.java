package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetSLCommandTest extends CommonCommandForTest {

    private final double newSL = 1.2345;

    @Before
    public void setUp() {
        command = new SetSLCommand(orderUnderTest, newSL);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderUnderTest).setStopLossPrice(newSL);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeSLData);
    }
}
