package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

public class SubmitCommandTest extends CommonUtilForTest {

    private OrderCallCommand command;
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
        final IOrder expectedOrder = IOrderForTest.buyOrderEURUSD();
        engineForTest.setSubmitExpectation(orderParams, expectedOrder);

        final Callable<IOrder> callable = command.callable();
        final IOrder order = callable.call();

        assertThat(order, equalTo(expectedOrder));
        engineForTest.verifySubmit(orderParams, 1);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertThat(command.orderEventTypeData(), equalTo(OrderEventTypeData.submitData));
    }
}
