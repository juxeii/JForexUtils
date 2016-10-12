package com.jforex.programming.order.command.test;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.BatchMode;
import com.jforex.programming.order.OrderChangeBatch;
import com.jforex.programming.order.OrderMergeTask;
import com.jforex.programming.order.command.CloseExecutionMode;
import com.jforex.programming.order.command.ClosePositionCommand;
import com.jforex.programming.order.command.ClosePositionCommandHandler;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class ClosePositionCommandHandlerTest extends InstrumentUtilForTest {

    private ClosePositionCommandHandler commandHandler;

    @Mock
    private OrderMergeTask orderMergeTaskMock;
    @Mock
    private OrderChangeBatch orderChangeBatchMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ClosePositionCommand closeCommandMock;
    @Mock
    private MergeCommand mergeCommandMock;
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

        commandHandler = new ClosePositionCommandHandler(orderMergeTaskMock,
                                                         orderChangeBatchMock,
                                                         positionUtilMock);
    }

    private void setUpMocks() {
        when(closeCommandMock.instrument()).thenReturn(instrumentEURUSD);
        when(closeCommandMock.closeBatchMode()).thenReturn(BatchMode.MERGE);

        when(positionUtilMock.filledOrders(instrumentEURUSD)).thenReturn(filledOrders);
        when(positionUtilMock.filledOrOpenedOrders(instrumentEURUSD)).thenReturn(allOrders);
        when(positionUtilMock.openedOrders(instrumentEURUSD)).thenReturn(openOrders);
    }

    private void setExecutionMode(final CloseExecutionMode mode) {
        when(closeCommandMock.executionMode()).thenReturn(mode);
    }

    public class ObserveMerge {

        @Before
        public void setUp() {
            when(closeCommandMock.maybeMergeCommand()).thenReturn(Optional.of(mergeCommandMock));
            when(orderMergeTaskMock.merge(filledOrders, mergeCommandMock))
                .thenReturn(eventObservable(testEvent));
        }

        private void setExecutionModeAndSubscribe(final CloseExecutionMode mode) {
            setExecutionMode(mode);

            testObserver = commandHandler
                .observeMerge(closeCommandMock)
                .test();
        }

        @Test
        public void observeMergeIsDeferred() {
            commandHandler.observeMerge(closeCommandMock);

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

            verify(orderMergeTaskMock).merge(filledOrders, mergeCommandMock);
            testObserver.assertComplete();
            testObserver.assertValue(testEvent);
        }

        @Test
        public void observeMergeForFilledOrOpenedOrdersReturnsObservableFromOrderMergeTask() {
            setExecutionModeAndSubscribe(CloseExecutionMode.CloseAll);

            verify(orderMergeTaskMock).merge(filledOrders, mergeCommandMock);
            testObserver.assertComplete();
            testObserver.assertValue(testEvent);
        }
    }

    public class ObserveClose {

        @Before
        public void setUp() {
            when(closeCommandMock.singleCloseComposer(any())).thenReturn(testComposer);
            when(closeCommandMock.closeAllComposer()).thenReturn(testComposer);
            when(closeCommandMock.closeFilledComposer()).thenReturn(testComposer);
            when(closeCommandMock.closeOpenedComposer()).thenReturn(testComposer);
        }

        public void setChangeBatchMock(final Collection<IOrder> orders) {
            when(orderChangeBatchMock.close(eq(orders),
                                            eq(BatchMode.MERGE),
                                            any()))
                                                .thenReturn(eventObservable(testEvent));
        }

        private void setExecutionModeAndSubscribe(final CloseExecutionMode mode) {
            setExecutionMode(mode);

            testObserver = commandHandler
                .observeClose(closeCommandMock)
                .test();
        }

        @Test
        public void observeCloseIsDeferred() {
            commandHandler.observeClose(closeCommandMock);

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
