package com.jforex.programming.order.command.test;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SetGTTCommand;

public class SetGTTCommandTest extends CommonCommandForTest {

    private final long newGTT = 1234L;

    @Before
    public void setUp() {
        command = new SetGTTCommand(orderForTest, newGTT);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setGoodTillTime(newGTT);
    }

    @Test
    public void filterIsFalseWhenNewGTTAlreadySet() {
        orderUtilForTest.setGTT(orderForTest, newGTT);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenNewGTTDiffers() {
        orderUtilForTest.setGTT(orderForTest, newGTT + 1L);

        assertFilterIsSet();
    }
}
