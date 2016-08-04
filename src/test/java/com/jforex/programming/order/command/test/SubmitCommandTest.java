package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.test.common.OrderUtilForTest;

public class SubmitCommandTest extends CommonCommandForTest {

    private final OrderParams orderParams = OrderUtilForTest.paramsBuyEURUSD();

    @Before
    public void setUp() {
        command = new SubmitCommand(orderParams, engineMock);
        command.logOnSubscribe();
        command.logOnError(jfException);
        command.logOnCompleted();
    }

    @Test
    public void callableIsCorrect() throws Exception {
        when(engineMock.submitOrder(orderParams.label(),
                                    orderParams.instrument(),
                                    orderParams.orderCommand(),
                                    orderParams.amount(),
                                    orderParams.price(),
                                    orderParams.slippage(),
                                    orderParams.stopLossPrice(),
                                    orderParams.takeProfitPrice(),
                                    orderParams.goodTillTime(),
                                    orderParams.comment())).thenReturn(orderForTest);

        assertCallableOrder();

        verify(engineMock).submitOrder(orderParams.label(),
                                       orderParams.instrument(),
                                       orderParams.orderCommand(),
                                       orderParams.amount(),
                                       orderParams.price(),
                                       orderParams.slippage(),
                                       orderParams.stopLossPrice(),
                                       orderParams.takeProfitPrice(),
                                       orderParams.goodTillTime(),
                                       orderParams.comment());
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertThat(command.orderEventTypeData(), equalTo(OrderEventTypeData.submitData));
    }
}
