package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.test.common.CommonUtilForTest;

public class IEngineUtilTest extends CommonUtilForTest {

    private IEngineUtil iengineUtil;

    @Before
    public void setUp() {
        iengineUtil = new IEngineUtil(engineMock);
    }

    @Test
    public void submitCallableIsCorrect() throws Exception {
        when(engineMock.submitOrder(buyParamsEURUSD.label(),
                                    buyParamsEURUSD.instrument(),
                                    buyParamsEURUSD.orderCommand(),
                                    buyParamsEURUSD.amount(),
                                    buyParamsEURUSD.price(),
                                    buyParamsEURUSD.slippage(),
                                    buyParamsEURUSD.stopLossPrice(),
                                    buyParamsEURUSD.takeProfitPrice(),
                                    buyParamsEURUSD.goodTillTime(),
                                    buyParamsEURUSD.comment()))
                                        .thenReturn(buyOrderEURUSD);
        final Callable<IOrder> callable = iengineUtil.submitCallable(buyParamsEURUSD);
        final IOrder order = callable.call();

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
        assertThat(order, equalTo(buyOrderEURUSD));
    }

    @Test
    public void mergeCallableIsCorrect() throws Exception {
        final String mergeOrderLabel = "mergeOrderLabel";
        final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
        when(engineMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(buyOrderEURUSD);

        final Callable<IOrder> callable = iengineUtil.mergeCallable(mergeOrderLabel, toMergeOrders);
        final IOrder order = callable.call();

        verify(engineMock).mergeOrders(mergeOrderLabel, toMergeOrders);
        assertThat(order, equalTo(buyOrderEURUSD));
    }
}
