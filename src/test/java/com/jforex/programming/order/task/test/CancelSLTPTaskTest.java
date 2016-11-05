package com.jforex.programming.order.task.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.MergeExecutionMode;
import com.jforex.programming.order.task.params.ComplexMergeParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CancelSLTPTaskTest extends InstrumentUtilForTest {

    private CancelSLTPTask cancelSLTPTask;

    @Mock
    private BatchChangeTask batchChangeTaskMock;
    @Mock
    private ComplexMergeParams mergeCommandMock;
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toCancelSLTPOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = mergeEvent;
    private final OrderEvent composerEvent = closeEvent;
    private final OrderEventTransformer testComposer =
            upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));

    @Before
    public void setUp() {
        cancelSLTPTask = new CancelSLTPTask(batchChangeTaskMock);
    }

    private void setUpCommandObservables(final Observable<OrderEvent> cancelSLObservable,
                                         final Observable<OrderEvent> cancelTPObservable) {
        when(batchChangeTaskMock.cancelSL(eq(toCancelSLTPOrders), any(), any()))
            .thenReturn(cancelSLObservable);
        when(batchChangeTaskMock.cancelTP(eq(toCancelSLTPOrders), any(), any()))
            .thenReturn(cancelTPObservable);
    }

    private void subscribeWithOrders(final Set<IOrder> orders) {
        testObserver = cancelSLTPTask
            .observe(orders, mergeCommandMock)
            .test();
    }

    @Test
    public void batchTaskIsDeferred() {
        verifyZeroInteractions(batchChangeTaskMock);
    }

    @Test
    public void observeTaskIsEmptyForNoOrders() {
        subscribeWithOrders(Sets.newHashSet());

        testObserver.assertComplete();
        testObserver.assertNoValues();
    }

    @Test
    public void observeTaskIsEmptyForOnerders() {
        subscribeWithOrders(Sets.newHashSet(buyOrderEURUSD));

        testObserver.assertComplete();
        testObserver.assertNoValues();
    }

    public class ObserveTaskSetup {

        @Before
        public void setUp() {
            when(mergeCommandMock.cancelSLTPComposer())
                .thenReturn(testComposer);
        }

        private void setExecutionMode(final MergeExecutionMode mode) {
            when(mergeCommandMock.executionMode()).thenReturn(mode);
        }

        @Test
        public void cancelSLAndCancelTPAreConcatenated() {
            setExecutionMode(MergeExecutionMode.ConcatCancelSLAndTP);
            setUpCommandObservables(neverObservable(), eventObservable(testEvent));

            subscribeWithOrders(toCancelSLTPOrders);

            testObserver.assertNotComplete();
            testObserver.assertNoValues();
        }

        @Test
        public void cancelTPAndCancelSLAreConcatenated() {
            setExecutionMode(MergeExecutionMode.ConcatCancelTPAndSL);
            setUpCommandObservables(eventObservable(testEvent), neverObservable());

            subscribeWithOrders(toCancelSLTPOrders);

            testObserver.assertNotComplete();
            testObserver.assertNoValues();
        }

        @Test
        public void cancelSLAndTPAreMerged() {
            setExecutionMode(MergeExecutionMode.MergeCancelSLAndTP);
            setUpCommandObservables(neverObservable(), eventObservable(testEvent));

            subscribeWithOrders(toCancelSLTPOrders);

            testObserver.assertNotComplete();
            testObserver.assertValue(composerEvent);
        }
    }
}
