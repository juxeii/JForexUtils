package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetGTTCommandTest extends CommonCommandForTest {

    private final long newGTT = 1234L;

    @Before
    public void setUp() {
        command = new SetGTTCommand(orderUnderTest, newGTT);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderUnderTest).setGoodTillTime(newGTT);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertOrderEventTypeData(OrderEventTypeData.changeGTTData);
    }
}
