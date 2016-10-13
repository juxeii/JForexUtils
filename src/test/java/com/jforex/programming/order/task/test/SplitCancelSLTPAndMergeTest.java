package com.jforex.programming.order.task.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergeCommandHandler;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.SplitCancelSLTPAndMerge;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class SplitCancelSLTPAndMergeTest extends InstrumentUtilForTest {

    private SplitCancelSLTPAndMerge splitter;

    @Mock
    private MergeCommandHandler commandHandlerMock;
    @Mock
    private MergeCommand mergeCommandMock;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = mergeEvent;

    @Before
    public void setUp() {
        setUpMocks();

        splitter = new SplitCancelSLTPAndMerge(commandHandlerMock);
    }

    private void setUpMocks() {
        when(commandHandlerMock.observeCancelSLTP(toMergeOrders, mergeCommandMock))
            .thenReturn(neverObservable());
        when(commandHandlerMock.observeMerge(toMergeOrders, mergeCommandMock))
            .thenReturn(eventObservable(testEvent));
    }

    private TestObserver<OrderEvent> testSubscribeSplitter() {
        return splitter
            .observe(toMergeOrders, mergeCommandMock)
            .test();
    }

    @Test
    public void cancelSLTPAndMergeAreConcatenated() {
        testSubscribeSplitter()
            .assertNotComplete()
            .assertNoValues();
    }

    @Test
    public void cancelSLTPIsCalledOnHandler() {
        testSubscribeSplitter();

        verify(commandHandlerMock).observeCancelSLTP(toMergeOrders, mergeCommandMock);
    }

    @Test
    public void mergeIsCalledOnHandler() {
        testSubscribeSplitter();

        verify(commandHandlerMock).observeMerge(toMergeOrders, mergeCommandMock);
    }
}
