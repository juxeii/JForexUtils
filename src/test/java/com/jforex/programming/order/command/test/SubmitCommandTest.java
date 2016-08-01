package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.fakes.IOrderForTest;

public class SubmitCommandTest extends CommonCommandForTest {

    private final OrderParams orderParams = IOrderForTest.paramsBuyEURUSD();

    @Before
    public void setUp() {
        command = new SubmitCommand(orderParams, engineMock);
        command.logOnSubscribe();
        command.logOnError(jfException);
        command.logOnCompleted();
    }

    @Test
    public void callableIsCorrect() throws Exception {
        engineForTest.setSubmitExpectation(orderParams, orderForTest);

        assertCallableOrder();
        engineForTest.verifySubmit(orderParams, 1);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertThat(command.orderEventTypeData(), equalTo(OrderEventTypeData.submitData));
    }
}
