package com.jforex.programming.order.task.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.CancelSLTPAndMergeTask;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParamsHandler;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CancelSLTPAndMergeTaskTest extends InstrumentUtilForTest {

    private CancelSLTPAndMergeTask cancelSLTPAndMergeTask;

    @Mock
    private MergePositionParamsHandler paramsHandlerMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = mergeEvent;

    @Before
    public void setUp() {
        setUpMocks();

        cancelSLTPAndMergeTask = new CancelSLTPAndMergeTask(paramsHandlerMock);
    }

    private void setUpMocks() {
        when(paramsHandlerMock.observeCancelSLTP(toMergeOrders, mergePositionParamsMock))
            .thenReturn(neverObservable());
        when(paramsHandlerMock.observeMerge(toMergeOrders, mergePositionParamsMock))
            .thenReturn(eventObservable(testEvent));
    }

    private TestObserver<OrderEvent> testSubscribeSplitter() {
        return cancelSLTPAndMergeTask
            .observe(toMergeOrders, mergePositionParamsMock)
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

        verify(paramsHandlerMock).observeCancelSLTP(toMergeOrders, mergePositionParamsMock);
    }

    @Test
    public void mergeIsCalledOnHandler() {
        testSubscribeSplitter();

        verify(paramsHandlerMock).observeMerge(toMergeOrders, mergePositionParamsMock);
    }
}
