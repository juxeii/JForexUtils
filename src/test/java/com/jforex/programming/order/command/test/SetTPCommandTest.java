package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetTPCommandTest extends CommonCommandForTest {

    private final double newTP = 1.2345;

    @Before
    public void setUp() {
        command = new SetTPCommand(orderForTest, newTP);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setTakeProfitPrice(newTP);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeTPData);
    }

    @Test
    public void filterIsFalseWhenNewTPAlreadySet() {
        orderUtilForTest.setTP(orderForTest, newTP);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenNewTPDiffers() {
        orderUtilForTest.setTP(orderForTest, newTP + 0.1);

        assertFilterIsSet();
    }
}
