package com.jforex.programming.order.command.test;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.command.MergeCommand;

public class MergeCommandTest extends CommonCommandForTest {

    private final String mergeOrderLabel = "mergeOrderLabel";
    private final Collection<IOrder> toMergeOrders =
            Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        command = new MergeCommand(mergeOrderLabel, toMergeOrders, engineMock);
        command.logOnSubscribe();
        command.logOnError(jfException);
        command.logOnCompleted();
    }

    @Test
    public void callableIsCorrect() throws Exception {
        when(engineMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(orderForTest);

        assertCallableOrder();
        verify(engineMock).mergeOrders(mergeOrderLabel, toMergeOrders);
    }
}
