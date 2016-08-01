package com.jforex.programming.test.fakes;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jforex.programming.order.OrderParams;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class IEngineForTest {

    private final IEngine engineMock;

    public IEngineForTest(final IEngine engineMock) {
        this.engineMock = engineMock;
    }

    public void setSubmitExpectation(final OrderParams orderParams,
                                     final IOrder order) throws JFException {
        when(engineMock.submitOrder(orderParams.label(),
                                    orderParams.instrument(),
                                    orderParams.orderCommand(),
                                    orderParams.amount(),
                                    orderParams.price(),
                                    orderParams.slippage(),
                                    orderParams.stopLossPrice(),
                                    orderParams.takeProfitPrice(),
                                    orderParams.goodTillTime(),
                                    orderParams.comment())).thenReturn(order);
    }

    public void verifySubmit(final OrderParams orderParams,
                             final int times) throws JFException {
        verify(engineMock, times(times)).submitOrder(orderParams.label(),
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
}
