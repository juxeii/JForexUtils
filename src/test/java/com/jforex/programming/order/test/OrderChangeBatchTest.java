package com.jforex.programming.order.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.jforex.programming.order.BatchMode;
import com.jforex.programming.order.OrderBasicTask;
import com.jforex.programming.order.OrderChangeBatch;
import com.jforex.programming.order.OrderEventTransformer;
import com.jforex.programming.order.OrderToEventTransformer;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class OrderChangeBatchTest extends InstrumentUtilForTest {

    private OrderChangeBatch orderChangeBatch;

    @Mock
    private OrderBasicTask orderBasicTaskMock;
    private final List<IOrder> ordersForBatch = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = submitEvent;
    private final OrderEvent composerEvent = closeEvent;
    private final OrderEventTransformer testComposer =
            upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));
    private final OrderToEventTransformer testOrderComposer =
            order -> testComposer;

    @Before
    public void setUp() {
        orderChangeBatch = new OrderChangeBatch(orderBasicTaskMock);
    }

    public class CloseBatch {

        @Before
        public void setUp() {
            when(orderBasicTaskMock.close(buyOrderEURUSD))
                .thenReturn(neverObservable());
            when(orderBasicTaskMock.close(sellOrderEURUSD))
                .thenReturn(eventObservable(testEvent));
        }

        @Test
        public void forMergeIsNotConcatenated() {
            orderChangeBatch
                .close(ordersForBatch,
                       BatchMode.MERGE,
                       testOrderComposer)
                .test()
                .assertNotComplete()
                .assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            orderChangeBatch
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
            when(orderBasicTaskMock.setStopLossPrice(buyOrderEURUSD, noSL))
                .thenReturn(neverObservable());
            when(orderBasicTaskMock.setStopLossPrice(sellOrderEURUSD, noSL))
                .thenReturn(eventObservable(testEvent));
        }

        @Test
        public void forMergeIsNotConcatenated() {
            orderChangeBatch
                .cancelSL(ordersForBatch,
                          BatchMode.MERGE,
                          testOrderComposer)
                .test()
                .assertNotComplete()
                .assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            orderChangeBatch
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
            when(orderBasicTaskMock.setTakeProfitPrice(buyOrderEURUSD, noTP))
                .thenReturn(neverObservable());
            when(orderBasicTaskMock.setTakeProfitPrice(sellOrderEURUSD, noTP))
                .thenReturn(eventObservable(testEvent));
        }

        @Test
        public void forMergeIsNotConcatenated() {
            orderChangeBatch
                .cancelTP(ordersForBatch,
                          BatchMode.MERGE,
                          testOrderComposer)
                .test()
                .assertNotComplete()
                .assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            orderChangeBatch
                .cancelTP(ordersForBatch,
                          BatchMode.CONCAT,
                          testOrderComposer)
                .test()
                .assertNotComplete()
                .assertNoValues();
        }
    }
}
