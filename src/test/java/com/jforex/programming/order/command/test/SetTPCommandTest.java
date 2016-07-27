package com.jforex.programming.order.command.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.OrderChangeCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetTPCommandTest extends CommonCommandForTest {

    private final double newTP = 1.2345;

    @Before
    public void setUp() {
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

    @Test
    public void filterIsFalseWhenNewTPAlreadySet() {
        orderUnderTest.setTakeProfitPrice(newTP);

        assertFalse(((OrderChangeCommand<?>) command).filter(orderUnderTest));
    }

    @Test
    public void filterIsTrueWhenNewTPDiffers() {
        orderUnderTest.setTakeProfitPrice(newTP + 0.1);

        assertTrue(((OrderChangeCommand<?>) command).filter(orderUnderTest));
    }
}
