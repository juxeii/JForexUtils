package com.jforex.programming.order.task.params.position.test;

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
import com.jforex.programming.order.task.MergePositionTask;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.ClosePositionParamsHandler;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.QuoteProviderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class ClosePositionParamsHandlerTest extends QuoteProviderForTest {

    private ClosePositionParamsHandler closePositionParamsHandler;

    @Mock
    private MergePositionTask mergePositionTaskObservableMock;
    @Mock
    private BatchChangeTask batchChangeTaskMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ClosePositionParams closePositionParamsMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() {
        setUpMocks();

        closePositionParamsHandler = new ClosePositionParamsHandler(mergePositionTaskObservableMock,
                                                                    batchChangeTaskMock,
                                                                    positionUtilMock);
    }

    private void setUpMocks() {
        when(closePositionParamsMock.instrument())
            .thenReturn(instrumentEURUSD);
        when(closePositionParamsMock.mergePositionParams())
            .thenReturn(mergePositionParamsMock);
    }

    @Test
    public void observeMergeDelegatesToMergePositionTaskMock() {
        when(mergePositionTaskObservableMock.merge(anyCollection(), eq(mergePositionParamsMock)))
            .thenReturn(eventObservable(closeEvent));

        testObserver = closePositionParamsHandler
            .observeMerge(closePositionParamsMock)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(closeEvent);
    }

    @Test
    public void emptyMergeObservableIsReturnedWhenClosingOnlyOpenedOrders() {
        when(closePositionParamsMock.closeExecutionMode())
            .thenReturn(CloseExecutionMode.CloseOpened);

        testObserver = closePositionParamsHandler
            .observeMerge(closePositionParamsMock)
            .test();

        testObserver.assertComplete();
        testObserver.assertNoValues();
    }

    public class ObserveClose {

        private final Set<IOrder> filledOrders = Sets.newHashSet(buyOrderEURUSD);
        private final Set<IOrder> openedOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
        private final Observable<OrderEvent> returnedObservable = eventObservable(closeEvent);

        public class ForFilledOrders {

            @Before
            public void setUp() {
                when(closePositionParamsMock.closeExecutionMode())
                    .thenReturn(CloseExecutionMode.CloseFilled);

                when(positionUtilMock.filledOrders(instrumentEURUSD))
                    .thenReturn(filledOrders);

                when(batchChangeTaskMock.close(filledOrders, closePositionParamsMock))
                    .thenReturn(returnedObservable);

                testObserver = closePositionParamsHandler
                    .observeClose(closePositionParamsMock)
                    .test();
            }

            @Test
            public void observeCloseCallsBatchTaskMockCorrect() {
                verify(batchChangeTaskMock).close(filledOrders, closePositionParamsMock);
            }

            @Test
            public void emittedEventIsCorrect() {
                testObserver.assertComplete();
                testObserver.assertValue(closeEvent);
            }
        }

        public class ForOpenedOrders {

            @Before
            public void setUp() {
                when(closePositionParamsMock.closeExecutionMode())
                    .thenReturn(CloseExecutionMode.CloseOpened);

                when(positionUtilMock.openedOrders(instrumentEURUSD))
                    .thenReturn(openedOrders);

                when(batchChangeTaskMock.close(openedOrders, closePositionParamsMock))
                    .thenReturn(returnedObservable);

                testObserver = closePositionParamsHandler
                    .observeClose(closePositionParamsMock)
                    .test();
            }

            @Test
            public void observeCloseCallsBatchTaskMockCorrect() {
                verify(batchChangeTaskMock).close(openedOrders, closePositionParamsMock);
            }

            @Test
            public void emittedEventIsCorrect() {
                testObserver.assertComplete();
                testObserver.assertValue(closeEvent);
            }
        }

        public class ForAllOrders {

            @Before
            public void setUp() {
                when(closePositionParamsMock.closeExecutionMode())
                    .thenReturn(CloseExecutionMode.CloseAll);

                when(positionUtilMock.filledOrOpenedOrders(instrumentEURUSD))
                    .thenReturn(openedOrders);

                when(batchChangeTaskMock.close(openedOrders, closePositionParamsMock))
                    .thenReturn(returnedObservable);

                testObserver = closePositionParamsHandler
                    .observeClose(closePositionParamsMock)
                    .test();
            }

            @Test
            public void observeCloseCallsBatchTaskMockCorrect() {
                verify(batchChangeTaskMock).close(openedOrders, closePositionParamsMock);
            }

            @Test
            public void emittedEventIsCorrect() {
                testObserver.assertComplete();
                testObserver.assertValue(closeEvent);
            }
        }
    }
}
