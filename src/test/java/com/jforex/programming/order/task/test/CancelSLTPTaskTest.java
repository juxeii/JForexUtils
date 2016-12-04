package com.jforex.programming.order.task.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchCancelSLTask;
import com.jforex.programming.order.task.BatchCancelTPTask;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CancelSLTPTaskTest extends InstrumentUtilForTest {

    private CancelSLTPTask cancelSLTPTask;

    @Mock
    private BatchCancelSLTask cancelSLTaskMock;
    @Mock
    private BatchCancelTPTask cancelTPTaskMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toCancelSLTPOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = mergeEvent;

    @Before
    public void setUp() {
        cancelSLTPTask = new CancelSLTPTask(cancelSLTaskMock, cancelTPTaskMock);
    }

    private void setUpCommandObservables(final Observable<OrderEvent> cancelSLObservable,
                                         final Observable<OrderEvent> cancelTPObservable) {
        when(cancelSLTaskMock.observe(toCancelSLTPOrders, mergePositionParamsMock))
            .thenReturn(cancelSLObservable);
        when(cancelTPTaskMock.observe(toCancelSLTPOrders, mergePositionParamsMock))
            .thenReturn(cancelTPObservable);
    }

    private void subscribeWithOrders(final Set<IOrder> orders) {
        testObserver = cancelSLTPTask
            .observe(orders, mergePositionParamsMock)
            .test();
    }

    @Test
    public void cancelTasksAreDeferred() {
        verifyZeroInteractions(cancelSLTaskMock);
        verifyZeroInteractions(cancelTPTaskMock);
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

        private void setExecutionMode(final CancelSLTPMode mode) {
            when(mergePositionParamsMock.mergeExecutionMode()).thenReturn(mode);
        }

        @Test
        public void cancelSLAndCancelTPAreConcatenated() {
            setExecutionMode(CancelSLTPMode.ConcatCancelSLAndTP);
            setUpCommandObservables(neverObservable(), eventObservable(testEvent));

            subscribeWithOrders(toCancelSLTPOrders);

            testObserver.assertNotComplete();
            testObserver.assertNoValues();
        }

        @Test
        public void cancelTPAndCancelSLAreConcatenated() {
            setExecutionMode(CancelSLTPMode.ConcatCancelTPAndSL);
            setUpCommandObservables(eventObservable(testEvent), neverObservable());

            subscribeWithOrders(toCancelSLTPOrders);

            testObserver.assertNotComplete();
            testObserver.assertNoValues();
        }

        @Test
        public void cancelSLAndTPAreMerged() {
            setExecutionMode(CancelSLTPMode.MergeCancelSLAndTP);
            setUpCommandObservables(neverObservable(), eventObservable(testEvent));

            subscribeWithOrders(toCancelSLTPOrders);

            testObserver.assertNotComplete();
            testObserver.assertValue(testEvent);
        }
    }
}
