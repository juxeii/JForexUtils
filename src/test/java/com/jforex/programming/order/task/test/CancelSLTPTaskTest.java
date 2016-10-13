package com.jforex.programming.order.task.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergeExecutionMode;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.CancelSLTask;
import com.jforex.programming.order.task.CancelTPTask;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CancelSLTPTaskTest extends InstrumentUtilForTest {

    private CancelSLTPTask cancelSLTPTask;

    @Mock
    private CancelSLTask orderCancelSLMock;
    @Mock
    private CancelTPTask orderCancelTPMock;
    @Mock
    private MergeCommand mergeCommandMock;
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toCancelSLTPOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = mergeEvent;
    private final OrderEvent composerEvent = closeEvent;
    private final OrderEventTransformer testComposer =
            upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));

    @Before
    public void setUp() {
        cancelSLTPTask = new CancelSLTPTask(orderCancelSLMock, orderCancelTPMock);
    }

    private void setUpCommandObservables(final Observable<OrderEvent> cancelSLObservable,
                                         final Observable<OrderEvent> cancelTPObservable) {
        when(orderCancelSLMock.observe(toCancelSLTPOrders, mergeCommandMock))
            .thenReturn(cancelSLObservable);
        when(orderCancelTPMock.observe(toCancelSLTPOrders, mergeCommandMock))
            .thenReturn(cancelTPObservable);
    }

    private void subscribeWithOrders(final Set<IOrder> orders) {
        testObserver = cancelSLTPTask
            .observe(orders, mergeCommandMock)
            .test();
    }

    @Test
    public void observeTaskIsDeferred() {
        verifyZeroInteractions(orderCancelSLMock);
        verifyZeroInteractions(orderCancelTPMock);
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
            when(mergeCommandMock.executionMode())
                .thenReturn(mode);
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
