package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SubmitCommandTest extends CommonCommandForTest {

    @Before
    public void setUp() {
        command = new SubmitCommand(buyParamsEURUSD, engineMock);
        command.logOnSubscribe();
        command.logOnError(jfException);
        command.logOnCompleted();
    }

    @Test
    public void callableIsCorrect() throws Exception {
        when(engineMock.submitOrder(buyParamsEURUSD.label(),
                                    buyParamsEURUSD.instrument(),
                                    buyParamsEURUSD.orderCommand(),
                                    buyParamsEURUSD.amount(),
                                    buyParamsEURUSD.price(),
                                    buyParamsEURUSD.slippage(),
                                    buyParamsEURUSD.stopLossPrice(),
                                    buyParamsEURUSD.takeProfitPrice(),
                                    buyParamsEURUSD.goodTillTime(),
                                    buyParamsEURUSD.comment())).thenReturn(orderForTest);

        assertCallableOrder();

        verify(engineMock).submitOrder(buyParamsEURUSD.label(),
                                       buyParamsEURUSD.instrument(),
                                       buyParamsEURUSD.orderCommand(),
                                       buyParamsEURUSD.amount(),
                                       buyParamsEURUSD.price(),
                                       buyParamsEURUSD.slippage(),
                                       buyParamsEURUSD.stopLossPrice(),
                                       buyParamsEURUSD.takeProfitPrice(),
                                       buyParamsEURUSD.goodTillTime(),
                                       buyParamsEURUSD.comment());
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertThat(command.orderEventTypeData(), equalTo(OrderEventTypeData.submitData));
    }
}
