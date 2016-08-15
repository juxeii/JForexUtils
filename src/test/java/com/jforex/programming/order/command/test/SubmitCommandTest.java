package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SubmitCommand;

public class SubmitCommandTest extends CommonCommandForTest {

    @Before
    public void setUp() {
        command = new SubmitCommand(buyParamsEURUSD, engineMock);
    }

    @Test
    public void orderEventTestAreCorrect() {
        assertIsDoneEvent(FULLY_FILLED,
                          SUBMIT_CONDITIONAL_OK);

        assertIsRejectEvent(FILL_REJECTED,
                            SUBMIT_REJECTED);

        assertEventIsForCommand(FULLY_FILLED,
                                SUBMIT_CONDITIONAL_OK,
                                FILL_REJECTED,
                                SUBMIT_REJECTED,
                                SUBMIT_OK,
                                PARTIAL_FILL_OK);
    }

    @Test
    public void orderCallReasonIsCorrect() {
        assertCallReason(OrderCallReason.SUBMIT);
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
}
