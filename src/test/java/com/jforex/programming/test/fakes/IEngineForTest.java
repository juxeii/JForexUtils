package com.jforex.programming.test.fakes;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.order.OrderParams;

public class IEngineForTest {

    private final IEngine engineMock;

    public IEngineForTest(final IEngine engineMock) {
        this.engineMock = engineMock;
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

    public void verifyMerge(final String mergeLabel,
                            final Collection<IOrder> toMergeOrders,
                            final int times) throws JFException {
        verify(engineMock, times(times)).mergeOrders(mergeLabel, toMergeOrders);
    }
}
