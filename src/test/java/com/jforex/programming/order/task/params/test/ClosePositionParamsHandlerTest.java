package com.jforex.programming.order.task.params.test;

import java.util.Collection;
import java.util.Optional;
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
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.order.task.params.CloseParams;
import com.jforex.programming.order.task.params.ClosePositionParams;
import com.jforex.programming.order.task.params.ClosePositionParamsHandler;
import com.jforex.programming.order.task.params.ComplexMergeParams;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class ClosePositionParamsHandlerTest extends InstrumentUtilForTest {

    private ClosePositionParamsHandler paramsHandler;

    @Mock
    private MergeTask orderMergeTaskMock;
    @Mock
    private BatchChangeTask orderChangeBatchMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ClosePositionParams closeParamsMock;
    @Mock
    private Function<IOrder, CloseParams> closeParamsProviderMock;
    @Mock
    private ComplexMergeParams mergeParamsMock;
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> filledOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final Set<IOrder> openOrders = Sets.newHashSet(buyOrderEURUSD);
    private final Set<IOrder> allOrders = Sets.newHashSet(sellOrderEURUSD);
    private final OrderEvent testEvent = closeEvent;
    private final OrderEvent composerEvent = changedLabelEvent;
    private final OrderEventTransformer testComposer =
            upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));

    @Before
    public void setUp() {
        setUpMocks();

        paramsHandler = new ClosePositionParamsHandler(orderMergeTaskMock,
                                                       orderChangeBatchMock,
                                                       positionUtilMock);
    }

    private void setUpMocks() {
        when(closeParamsMock.instrument()).thenReturn(instrumentEURUSD);
        when(closeParamsMock.closeBatchMode()).thenReturn(BatchMode.MERGE);

        when(positionUtilMock.filledOrders(instrumentEURUSD)).thenReturn(filledOrders);
        when(positionUtilMock.filledOrOpenedOrders(instrumentEURUSD)).thenReturn(allOrders);
        when(positionUtilMock.openedOrders(instrumentEURUSD)).thenReturn(openOrders);
    }

    private void setExecutionMode(final CloseExecutionMode mode) {
        when(closeParamsMock.executionMode()).thenReturn(mode);
    }

    public class ObserveMerge {

        @Before
        public void setUp() {
            when(closeParamsMock.maybeMergeParams()).thenReturn(Optional.of(mergeParamsMock));
            when(orderMergeTaskMock.merge(filledOrders, mergeParamsMock))
                .thenReturn(eventObservable(testEvent));
        }

        private void setExecutionModeAndSubscribe(final CloseExecutionMode mode) {
            setExecutionMode(mode);

            testObserver = paramsHandler
                .observeMerge(closeParamsMock)
                .test();
        }

        @Test
        public void observeMergeIsDeferred() {
            paramsHandler.observeMerge(closeParamsMock);

            verifyZeroInteractions(orderMergeTaskMock);
            verifyZeroInteractions(orderChangeBatchMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @Test
        public void observeMergeReturnsEmptyObservableForCloseOpenedMode() {
            setExecutionModeAndSubscribe(CloseExecutionMode.CloseOpened);

            testObserver.assertComplete();
            testObserver.assertNoValues();
        }

        @Test
        public void observeMergeForFilledOrdersReturnsObservableFromOrderMergeTask() {
            setExecutionModeAndSubscribe(CloseExecutionMode.CloseFilled);

            verify(orderMergeTaskMock).merge(filledOrders, mergeParamsMock);
            testObserver.assertComplete();
            testObserver.assertValue(testEvent);
        }

        @Test
        public void observeMergeForFilledOrOpenedOrdersReturnsObservableFromOrderMergeTask() {
            setExecutionModeAndSubscribe(CloseExecutionMode.CloseAll);

            verify(orderMergeTaskMock).merge(filledOrders, mergeParamsMock);
            testObserver.assertComplete();
            testObserver.assertValue(testEvent);
        }
    }

    public class ObserveClose {

        @Before
        public void setUp() {
            when(closeParamsMock.closeParamsProvider()).thenReturn(closeParamsProviderMock);
            when(closeParamsMock.singleCloseComposer(any())).thenReturn(testComposer);
            when(closeParamsMock.closeAllComposer()).thenReturn(testComposer);
            when(closeParamsMock.closeFilledComposer()).thenReturn(testComposer);
            when(closeParamsMock.closeOpenedComposer()).thenReturn(testComposer);
        }

        public void setChangeBatchMock(final Collection<IOrder> orders) {
            when(orderChangeBatchMock.close(eq(orders),
                                            eq(closeParamsProviderMock),
                                            eq(BatchMode.MERGE),
                                            any()))
                                                .thenReturn(eventObservable(testEvent));
        }

        private void setExecutionModeAndSubscribe(final CloseExecutionMode mode) {
            setExecutionMode(mode);

            testObserver = paramsHandler
                .observeClose(closeParamsMock)
                .test();
        }

        @Test
        public void observeCloseIsDeferred() {
            paramsHandler.observeClose(closeParamsMock);

            verifyZeroInteractions(orderMergeTaskMock);
            verifyZeroInteractions(orderChangeBatchMock);
            verifyZeroInteractions(positionUtilMock);
        }

        public class CloseAll {

            @Before
            public void setUp() {
                setChangeBatchMock(allOrders);
                setExecutionModeAndSubscribe(CloseExecutionMode.CloseAll);
            }

            @Test
            public void orderChangeBatchIsCalledCorrect() {
                verify(orderChangeBatchMock).close(eq(allOrders),
                                                   eq(closeParamsProviderMock),
                                                   eq(BatchMode.MERGE),
                                                   any());
            }

            @Test
            public void returnedObservableIsCorrectComposed() {
                testObserver.assertComplete();
                testObserver.assertValue(composerEvent);
            }
        }

        public class CloseFilled {

            @Before
            public void setUp() {
                setChangeBatchMock(filledOrders);
                setExecutionModeAndSubscribe(CloseExecutionMode.CloseFilled);
            }

            @Test
            public void orderChangeBatchIsCalledCorrect() {
                verify(orderChangeBatchMock).close(eq(filledOrders),
                                                   eq(closeParamsProviderMock),
                                                   eq(BatchMode.MERGE),
                                                   any());
            }

            @Test
            public void returnedObservableIsCorrectComposed() {
                testObserver.assertComplete();
                testObserver.assertValue(composerEvent);
            }
        }

        public class CloseOpened {

            @Before
            public void setUp() {
                setChangeBatchMock(openOrders);
                setExecutionModeAndSubscribe(CloseExecutionMode.CloseOpened);
            }

            @Test
            public void orderChangeBatchIsCalledCorrect() {
                verify(orderChangeBatchMock).close(eq(openOrders),
                                                   eq(closeParamsProviderMock),
                                                   eq(BatchMode.MERGE),
                                                   any());
            }

            @Test
            public void returnedObservableIsCorrectComposed() {
                testObserver.assertComplete();
                testObserver.assertValue(composerEvent);
            }
        }
    }
}
