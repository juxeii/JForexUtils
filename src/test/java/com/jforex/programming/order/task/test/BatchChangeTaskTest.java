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
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class BatchChangeTaskTest extends InstrumentUtilForTest {

    private BatchChangeTask batchChangeTask;

    @Mock
    private BasicTask basicTaskMock;
    private final List<IOrder> ordersForBatch = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = submitEvent;
    private final OrderEvent composerEvent = closeEvent;
    private final OrderEventTransformer testComposer =
            upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));
    private final OrderToEventTransformer testOrderComposer =
            order -> testComposer;

    @Before
    public void setUp() {
        batchChangeTask = new BatchChangeTask(basicTaskMock);
    }

    public class CloseBatch {

        @Before
        public void setUp() {
            when(basicTaskMock.close(buyOrderEURUSD))
                .thenReturn(neverObservable());
            when(basicTaskMock.close(sellOrderEURUSD))
                .thenReturn(eventObservable(testEvent));
        }

        @Test
        public void forMergeIsNotConcatenated() {
            batchChangeTask
                .close(ordersForBatch,
                       BatchMode.MERGE,
                       testOrderComposer)
                .test()
                .assertNotComplete()
                .assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            batchChangeTask
                .close(ordersForBatch,
                       BatchMode.CONCAT,
                       testOrderComposer)
                .test()
                .assertNotComplete()
                .assertNoValues();
        }
    }

    public class CancelSLBatch {

        @Before
        public void setUp() {
            when(basicTaskMock.setStopLossPrice(buyOrderEURUSD, noSL))
                .thenReturn(neverObservable());
            when(basicTaskMock.setStopLossPrice(sellOrderEURUSD, noSL))
                .thenReturn(eventObservable(testEvent));
        }

        @Test
        public void forMergeIsNotConcatenated() {
            batchChangeTask
                .cancelSL(ordersForBatch,
                          BatchMode.MERGE,
                          testOrderComposer)
                .test()
                .assertNotComplete()
                .assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            batchChangeTask
                .cancelSL(ordersForBatch,
                          BatchMode.CONCAT,
                          testOrderComposer)
                .test()
                .assertNotComplete()
                .assertNoValues();
        }
    }

    public class CancelTPBatch {

        @Before
        public void setUp() {
            when(basicTaskMock.setTakeProfitPrice(buyOrderEURUSD, noTP))
                .thenReturn(neverObservable());
            when(basicTaskMock.setTakeProfitPrice(sellOrderEURUSD, noTP))
                .thenReturn(eventObservable(testEvent));
        }

        @Test
        public void forMergeIsNotConcatenated() {
            batchChangeTask
                .cancelTP(ordersForBatch,
                          BatchMode.MERGE,
                          testOrderComposer)
                .test()
                .assertNotComplete()
                .assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            batchChangeTask
                .cancelTP(ordersForBatch,
                          BatchMode.CONCAT,
                          testOrderComposer)
                .test()
                .assertNotComplete()
                .assertNoValues();
        }
    }
}
