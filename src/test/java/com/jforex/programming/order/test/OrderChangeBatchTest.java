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
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OrderChangeBatchTest extends InstrumentUtilForTest {

    private OrderChangeBatch orderChangeBatch;

    @Mock
    private OrderBasicTask orderBasicTaskMock;
    private final List<IOrder> ordersForBatch = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
    private TestObserver<OrderEvent> testObserver;
    private final OrderEvent testEvent = submitEvent;
    private final OrderEvent composerEvent = closeEvent;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> testComposer =
            obs -> obs.flatMap(orderEvent -> Observable.just(composerEvent));
    private final Function<IOrder,
                           Function<Observable<OrderEvent>,
                                    Observable<OrderEvent>>> testOrderComposer = order -> testComposer;

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
            testObserver = orderChangeBatch
                .close(ordersForBatch,
                       BatchMode.MERGE,
                       testOrderComposer)
                .test();

            testObserver.assertNotComplete();
            testObserver.assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            testObserver = orderChangeBatch
                .close(ordersForBatch,
                       BatchMode.CONCAT,
                       testOrderComposer)
                .test();

            testObserver.assertNotComplete();
            testObserver.assertNoValues();
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
            testObserver = orderChangeBatch
                .cancelSL(ordersForBatch,
                          BatchMode.MERGE,
                          testOrderComposer)
                .test();

            testObserver.assertNotComplete();
            testObserver.assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            testObserver = orderChangeBatch
                .cancelSL(ordersForBatch,
                          BatchMode.CONCAT,
                          testOrderComposer)
                .test();

            testObserver.assertNotComplete();
            testObserver.assertNoValues();
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
            testObserver = orderChangeBatch
                .cancelTP(ordersForBatch,
                          BatchMode.MERGE,
                          testOrderComposer)
                .test();

            testObserver.assertNotComplete();
            testObserver.assertValue(composerEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            testObserver = orderChangeBatch
                .cancelTP(ordersForBatch,
                          BatchMode.CONCAT,
                          testOrderComposer)
                .test();

            testObserver.assertNotComplete();
            testObserver.assertNoValues();
        }
    }
}
