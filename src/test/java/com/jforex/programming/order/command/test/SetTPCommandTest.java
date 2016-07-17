package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetTPCommandTest extends CommonCommandForTest {

    private final double newTP = 1.2345;

    @Before
    public void setUp() {
        setUpCommon();

        command = new SetTPCommand(orderUnderTest, newTP);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderUnderTest).setTakeProfitPrice(newTP);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeTPData);
    }
}
