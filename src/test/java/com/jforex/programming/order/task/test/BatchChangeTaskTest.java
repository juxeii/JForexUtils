package com.jforex.programming.order.task.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.event.OrderToEventTransformer;
import com.jforex.programming.order.task.BasicTaskObservable;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.CancelSLParams;
import com.jforex.programming.order.task.params.position.CancelTPParams;
import com.jforex.programming.order.task.params.position.SimpleClosePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class BatchChangeTaskTest extends InstrumentUtilForTest {

    private BatchChangeTask batchChangeTask;

    @Mock
    private BasicTaskObservable basicTaskMock;
    @Mock
    private TaskParamsUtil taskParamsUtilMock;
    @Mock
    private SimpleClosePositionParams simpleClosePositionParams;
    @Mock
    private CancelSLParams cancelSLParamsMock;
    @Mock
    private CancelTPParams cancelTPParamsMock;

    private final CloseParams closeParams = CloseParams
        .closeOrder(buyOrderEURUSD)
        .build();
    private final List<IOrder> ordersForBatch = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = submitEvent;
    private final OrderEvent composerEvent = closeEvent;
    private final OrderEventTransformer testComposer =
            upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));
    private final OrderToEventTransformer testOrderComposer = order -> testComposer;

    @Before
    public void setUp() throws Exception {
        batchChangeTask = new BatchChangeTask(basicTaskMock, taskParamsUtilMock);
    }

    public class CloseBatch {

        @Before
        public void setUp() {
            when(basicTaskMock.close(closeParams))
                .thenReturn(neverObservable())
                .thenReturn(eventObservable(testEvent));
        }

        @Test
        public void forMergeIsNotConcatenated() {
            batchChangeTask
                .close(instrumentEURUSD,
                       ordersForBatch,
                       simpleClosePositionParams)
                .test()
                .assertNotComplete()
                .assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            batchChangeTask
                .close(instrumentEURUSD,
                       ordersForBatch,
                       simpleClosePositionParams)
                .test()
                .assertNotComplete()
                .assertNoValues();
        }
    }

    public class CancelSLBatch {

        @Before
        public void setUp() {
            when(basicTaskMock.setStopLossPrice(any()))
                .thenReturn(neverObservable());
            when(basicTaskMock.setStopLossPrice(any()))
                .thenReturn(eventObservable(testEvent));
        }

        @Test
        public void forMergeIsNotConcatenated() {
            batchChangeTask
                .cancelSL(ordersForBatch,
                          cancelSLParamsMock,
                          BatchMode.MERGE)
                .test()
                .assertNotComplete()
                .assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            batchChangeTask
                .cancelSL(ordersForBatch,
                          cancelSLParamsMock,
                          BatchMode.CONCAT)
                .test()
                .assertNotComplete()
                .assertNoValues();
        }
    }

    public class CancelTPBatch {

        @Before
        public void setUp() {
            when(basicTaskMock.setTakeProfitPrice(any()))
                .thenReturn(neverObservable());
            when(basicTaskMock.setTakeProfitPrice(any()))
                .thenReturn(eventObservable(testEvent));
        }

        @Test
        public void forMergeIsNotConcatenated() {
            batchChangeTask
                .cancelSL(ordersForBatch,
                          cancelSLParamsMock,
                          BatchMode.MERGE)
                .test()
                .assertNotComplete()
                .assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            batchChangeTask
                .cancelSL(ordersForBatch,
                          cancelSLParamsMock,
                          BatchMode.CONCAT)
                .test()
                .assertNotComplete()
                .assertNoValues();
        }
    }
}
