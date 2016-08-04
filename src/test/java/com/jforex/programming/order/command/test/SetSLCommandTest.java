package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.common.OrderUtilForTest;

public class SetSLCommandTest extends CommonCommandForTest {

    private final double newSL = 1.2345;

    @Before
    public void setUp() {
        command = new SetSLCommand(orderForTest, newSL);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setStopLossPrice(newSL);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeSLData);
    }

    @Test
    public void filterIsFalseWhenNewSLAlreadySet() {
        OrderUtilForTest.setSL(orderForTest, newSL);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenNewSLDiffers() {
        OrderUtilForTest.setSL(orderForTest, newSL + 0.1);

        assertFilterIsSet();
    }
}
