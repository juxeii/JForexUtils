package com.jforex.programming.test.fakes;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import com.jforex.programming.order.OrderParams;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class IEngineForTest {

    private final IEngine engineMock;
    private final JFException jfException;

    public IEngineForTest(final IEngine engineMock,
                          final JFException jfException) {
        this.engineMock = engineMock;
        this.jfException = jfException;
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

    public void throwOnSubmit(final OrderParams orderParams) throws JFException {
        doThrow(jfException).when(engineMock)
                .submitOrder(orderParams.label(),
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

    public void throwOnMerge(final String mergeLabel,
                             final Collection<IOrder> ordersToMerge) throws JFException {
        doThrow(jfException).when(engineMock)
                .mergeOrders(mergeLabel, ordersToMerge);
    }
}
