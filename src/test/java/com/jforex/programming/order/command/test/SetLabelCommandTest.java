package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetLabelCommandTest extends CommonCommandForTest {

    private final String newLabel = "newLabel";

    @Before
    public void setUp() {
        command = new SetLabelCommand(orderForTest, newLabel);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setLabel(newLabel);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeLabelData);
    }

    @Test
    public void filterIsFalseWhenNewLabelAlreadySet() {
        orderForTest.setLabel(newLabel);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenNewLabelDiffers() {
        orderForTest.setLabel("Other" + newLabel);

        assertFilterIsSet();
    }
}
