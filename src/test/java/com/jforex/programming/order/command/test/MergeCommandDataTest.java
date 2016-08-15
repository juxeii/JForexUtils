package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.MergeCommandData;

public class MergeCommandDataTest extends CommonCommandForTest {

    private final String mergeOrderLabel = "mergeOrderLabel";
    private final Collection<IOrder> toMergeOrders =
            Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        commandData = new MergeCommandData(mergeOrderLabel, toMergeOrders, engineMock);
    }

    @Test
    public void orderEventTypesAreCorrect() {
        assertDoneOrderEventTypes(MERGE_OK,
                                  MERGE_CLOSE_OK);

        assertRejectOrderEventTypes(MERGE_REJECTED);

        assertAllOrderEventTypes(MERGE_OK,
                                 NOTIFICATION,
                                 MERGE_CLOSE_OK,
                                 MERGE_REJECTED);
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
