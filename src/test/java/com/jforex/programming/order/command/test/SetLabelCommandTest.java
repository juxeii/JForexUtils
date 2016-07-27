package com.jforex.programming.order.command.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.OrderChangeCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetLabelCommandTest extends CommonCommandForTest {

    private final String newLabel = "newLabel";

    @Before
    public void setUp() {
        command = new SetLabelCommand(orderUnderTest, newLabel);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderUnderTest).setLabel(newLabel);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeLabelData);
    }

    @Test
    public void filterIsFalseWhenNewLabelAlreadySet() {
        orderUnderTest.setLabel(newLabel);

        assertFalse(((OrderChangeCommand<?>) command).filter(orderUnderTest));
    }

    @Test
    public void filterIsTrueWhenNewLabelDiffers() {
        orderUnderTest.setLabel("Other" + newLabel);

        assertTrue(((OrderChangeCommand<?>) command).filter(orderUnderTest));
    }
}
