package com.jforex.programming.order.task.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.MergeAndClosePositionTask;
import com.jforex.programming.order.task.MergePositionTask;
import com.jforex.programming.order.task.OrdersForPositionClose;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.test.common.QuoteProviderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class MergeAndClosePositionTaskTest extends QuoteProviderForTest {

    private MergeAndClosePositionTask mergeAndClosePositionTask;

    @Mock
    private MergePositionTask mergePositionTaskObservableMock;
    @Mock
    private BatchChangeTask batchChangeTaskMock;
    @Mock
    private OrdersForPositionClose ordersForPositionCloseMock;
    @Mock
    private ClosePositionParams closePositionParamsMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() {
        setUpMocks();

        mergeAndClosePositionTask = new MergeAndClosePositionTask(mergePositionTaskObservableMock,
                                                                  batchChangeTaskMock,
                                                                  ordersForPositionCloseMock);
    }

    private void setUpMocks() {
        when(closePositionParamsMock.instrument())
            .thenReturn(instrumentEURUSD);
        when(closePositionParamsMock.mergePositionParams())
            .thenReturn(mergePositionParamsMock);
    }

    public class ObserveMerge {

        private final Set<IOrder> ordersForMerge = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

        private void assertMergeObservable(final Set<IOrder> orders) {
            when(ordersForPositionCloseMock.filled(instrumentEURUSD))
                .thenReturn(orders);

            testObserver = mergeAndClosePositionTask
                .observeMerge(closePositionParamsMock)
                .test();

            testObserver.assertComplete();
        }

        @Test
        public void observeMergeIsEmptyForNoFilledOrders() {
            assertMergeObservable(Sets.newHashSet());
            verifyZeroInteractions(mergePositionTaskObservableMock);
        }

        @Test
        public void observeMergeIsEmptyForOneFilledOrder() {
            assertMergeObservable(Sets.newHashSet(buyOrderEURUSD));
            verifyZeroInteractions(mergePositionTaskObservableMock);
        }

        @Test
        public void observeMergeDelegatesToMergePositionTaskMock() {
            when(mergePositionTaskObservableMock.merge(anyCollection(), eq(mergePositionParamsMock)))
                .thenReturn(eventObservable(closeEvent));

            assertMergeObservable(ordersForMerge);
            testObserver.assertValue(closeEvent);
        }

        @Test
        public void emptyMergeObservableIsReturnedWhenClosingOnlyOpenedOrders() {
            when(closePositionParamsMock.closeExecutionMode())
                .thenReturn(CloseExecutionMode.CloseOpened);

            testObserver = mergeAndClosePositionTask
                .observeMerge(closePositionParamsMock)
                .test();

            testObserver.assertComplete();
            testObserver.assertNoValues();
        }
    }

    public class ObserveClose {

        @Test
        public void observeCloseIsEmptyForNoOrders() {
            when(ordersForPositionCloseMock.forMode(closePositionParamsMock))
                .thenReturn(Sets.newHashSet());

            mergeAndClosePositionTask
                .observeClose(closePositionParamsMock)
                .test()
                .assertNoValues()
                .assertComplete();

            verifyZeroInteractions(batchChangeTaskMock);
        }

        public class WithOrdersToClose {

            private final Set<IOrder> filledOrders = Sets.newHashSet(buyOrderEURUSD);

            @Before
            public void setUp() {
                when(ordersForPositionCloseMock.forMode(closePositionParamsMock))
                    .thenReturn(filledOrders);

                when(batchChangeTaskMock.close(filledOrders, closePositionParamsMock))
                    .thenReturn(eventObservable(closeEvent));

                testObserver = mergeAndClosePositionTask
                    .observeClose(closePositionParamsMock)
                    .test();
            }

            @Test
            public void observeCloseCallsBatchTaskMockCorrect() {
                verify(batchChangeTaskMock).close(filledOrders, closePositionParamsMock);
            }

            @Test
            public void ordersForPositionCloseIsCalled() {
                verify(ordersForPositionCloseMock).forMode(closePositionParamsMock);
            }

            @Test
            public void emittedEventIsCorrect() {
                testObserver.assertComplete();
                testObserver.assertValue(closeEvent);
            }
        }
    }
}
