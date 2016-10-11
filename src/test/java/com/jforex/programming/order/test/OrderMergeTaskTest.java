package com.jforex.programming.order.test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderMergeTask;
import com.jforex.programming.order.SplitCancelSLTPAndMerge;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OrderMergeTaskTest extends InstrumentUtilForTest {

    private OrderMergeTask orderMergeTask;

    @Mock
    private SplitCancelSLTPAndMerge splitterMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private MergeCommand mergeCommandMock;
    @Mock
    private Function<Instrument, MergeCommand> commandFactoryMock;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = mergeEvent;
    private Observable<OrderEvent> testObservable;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() {
        setUpMocks();

        orderMergeTask = new OrderMergeTask(splitterMock, positionUtilMock);
    }

    private void setUpMocks() {
        when(positionUtilMock.filledOrders(instrumentEURUSD))
            .thenReturn(toMergeOrders);
    }

    private void setUpSplitterObservable(final Observable<OrderEvent> splitterObservable) {
        when(splitterMock.observe(toMergeOrders, mergeCommandMock))
            .thenReturn(splitterObservable);
    }

    public class MergeCallSetup {

        @Before
        public void setUp() {
            testObservable = orderMergeTask.merge(toMergeOrders, mergeCommandMock);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(splitterMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @Test
        public void splitterMockIsCalledCorrect() {
            setUpSplitterObservable(eventObservable(testEvent));

            testObservable
                .test()
                .assertValue(testEvent);

            verify(splitterMock).observe(toMergeOrders, mergeCommandMock);
        }
    }

    public class MergePositionCallSetup {

        @Before
        public void setUp() {
            testObservable = orderMergeTask.mergePosition(instrumentEURUSD, mergeCommandMock);
        }

        @Test
        public void mergePositionCallIsDeferred() {
            verifyZeroInteractions(splitterMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @Test
        public void splitterMockIsCalledCorrect() {
            setUpSplitterObservable(eventObservable(testEvent));

            testObservable
                .test()
                .assertValue(testEvent);

            verify(splitterMock).observe(toMergeOrders, mergeCommandMock);
            verify(positionUtilMock).filledOrders(instrumentEURUSD);
        }
    }

    public class MergeAllCallSetup {

        private List<Observable<OrderEvent>> closeObservables;

        @Before
        public void setUp() throws Exception {
            when(commandFactoryMock.apply(instrumentEURUSD)).thenReturn(mergeCommandMock);
        }

        private void mergeAllSubscribe() {
            testObserver = orderMergeTask
                .mergeAllPositions(commandFactoryMock)
                .test();
        }

        private void setUpPositionUtilObservables(final Observable<OrderEvent> firstObservable,
                                                  final Observable<OrderEvent> secondObservable) {
            closeObservables = Stream
                .of(firstObservable, secondObservable)
                .collect(Collectors.toList());
            when(positionUtilMock.observablesFromFactory(any())).thenReturn(closeObservables);

            mergeAllSubscribe();
        }

        @Test
        public void mergePositionCallIsDeferred() {
            verifyZeroInteractions(splitterMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @SuppressWarnings("unchecked")
        @Test
        public void verifyThatPositionUtilIsCalledWithCorrectFactory() throws Exception {
            doAnswer(invocation -> ((Function<Instrument, Observable<OrderEvent>>) invocation.getArgument(0))
                .apply(instrumentEURUSD)
                .subscribe())
                    .when(positionUtilMock).observablesFromFactory(any());

            setUpSplitterObservable(emptyObservable());
            mergeAllSubscribe();

            verify(commandFactoryMock).apply(instrumentEURUSD);
            verify(splitterMock).observe(toMergeOrders, mergeCommandMock);
        }

        @Test
        public void verifyThatMergeCommandsAreMerged() {
            setUpPositionUtilObservables(neverObservable(), eventObservable(testEvent));

            testObserver.assertValue(testEvent);
            testObserver.assertNotComplete();
        }

        @Test
        public void completesWhenAllSingleCommandsComplete() {
            setUpPositionUtilObservables(emptyObservable(), emptyObservable());

            testObserver.assertComplete();
        }
    }
}
