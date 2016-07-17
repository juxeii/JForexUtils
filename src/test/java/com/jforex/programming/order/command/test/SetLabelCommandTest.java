package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetLabelCommandTest extends CommonCommandForTest {

    private final String newLabel = "newLabel";

    @Before
    public void setUp() {
        setUpCommon();

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
}
