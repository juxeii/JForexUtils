package com.jforex.programming.order.command.test;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEventTypeData;

public class MergeCommandTest extends CommonCommandForTest {

    private final String mergeOrderLabel = "mergeOrderLabel";
    private final Collection<IOrder> toMergeOrders =
            Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        command = new MergeCommand(mergeOrderLabel, toMergeOrders, engineMock);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertEventTypeData(OrderEventTypeData.mergeEventTypeData);
    }

    @Test
    public void orderCallReasonIsCorrect() {
        assertCallReason(OrderCallReason.MERGE);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        when(engineMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(orderForTest);

        assertCallableOrder();
        verify(engineMock).mergeOrders(mergeOrderLabel, toMergeOrders);
    }
}
