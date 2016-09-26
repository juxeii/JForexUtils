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
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergeCommandHandler;
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
    private MergeCommandHandler commandHandlerMock;
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

        orderMergeTask = new OrderMergeTask(commandHandlerMock, positionUtilMock);
    }

    private void setUpMocks() {
        when(positionUtilMock.filledOrders(instrumentEURUSD))
            .thenReturn(toMergeOrders);
    }

    private void setUpCommandObservables(final Observable<OrderEvent> cancelSLTPObservable,
                                         final Observable<OrderEvent> mergeObservable) {
        when(commandHandlerMock.observeCancelSLTP(toMergeOrders, mergeCommandMock))
            .thenReturn(cancelSLTPObservable);
        when(commandHandlerMock.observeMerge(toMergeOrders, mergeCommandMock))
            .thenReturn(mergeObservable);
    }

    public class MergeCallSetup {

        @Before
        public void setUp() {
            testObservable = orderMergeTask.merge(toMergeOrders, mergeCommandMock);
        }

        @Test
        public void callIsDeferred() {
            verifyZeroInteractions(commandHandlerMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @Test
        public void cancelSLTPAndMergeAreConcatenated() {
            setUpCommandObservables(neverObservable(), eventObservable(testEvent));

            testObserver = testObservable.test();

            testObserver.assertNotComplete();
            testObserver.assertNoValues();
        }
    }

    public class MergePositionCallSetup {

        @Before
        public void setUp() {
            testObservable = orderMergeTask.mergePosition(instrumentEURUSD, mergeCommandMock);
        }

        @Test
        public void mergePositionCallIsDeferred() {
            verifyZeroInteractions(commandHandlerMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @Test
        public void cancelSLTPAndMergeAreConcatenated() {
            setUpCommandObservables(neverObservable(), eventObservable(testEvent));

            testObserver = testObservable.test();

            testObserver.assertNotComplete();
            testObserver.assertNoValues();
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
                .mergeAll(commandFactoryMock)
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
            verifyZeroInteractions(commandHandlerMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @SuppressWarnings("unchecked")
        @Test
        public void verifyThatPositionUtilIsCalledWithCorrectFactory() throws Exception {
            doAnswer(invocation -> ((Function<Instrument, Observable<OrderEvent>>) invocation.getArgument(0))
                .apply(instrumentEURUSD)
                .subscribe())
                    .when(positionUtilMock).observablesFromFactory(any());

            setUpCommandObservables(emptyObservable(), emptyObservable());
            mergeAllSubscribe();

            verify(commandFactoryMock).apply(instrumentEURUSD);
            verify(commandHandlerMock).observeCancelSLTP(toMergeOrders, mergeCommandMock);
            verify(commandHandlerMock).observeMerge(toMergeOrders, mergeCommandMock);
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
