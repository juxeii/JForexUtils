package com.jforex.programming.order.task.test;

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
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.CancelSLTPAndMergeTask;
import com.jforex.programming.order.task.ComplexMergeTask;
import com.jforex.programming.order.task.params.position.ComplexMergePositionParams;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class MergeTaskTest extends InstrumentUtilForTest {

    private ComplexMergeTask mergeTask;

    @Mock
    private CancelSLTPAndMergeTask splitterMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ComplexMergePositionParams mergeParamsMock;
    @Mock
    private Function<Instrument, ComplexMergePositionParams> paramsFactoryMock;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = mergeEvent;
    private Observable<OrderEvent> testObservable;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() {
        setUpMocks();

        mergeTask = new ComplexMergeTask(splitterMock, positionUtilMock);
    }

    private void setUpMocks() {
        when(positionUtilMock.filledOrders(instrumentEURUSD))
            .thenReturn(toMergeOrders);
    }

    private void setUpSplitterObservable(final Observable<OrderEvent> splitterObservable) {
        when(splitterMock.observe(toMergeOrders, mergeParamsMock))
            .thenReturn(splitterObservable);
    }

    public class MergeCallSetup {

        @Before
        public void setUp() {
            testObservable = mergeTask.merge(toMergeOrders, mergeParamsMock);
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

            verify(splitterMock).observe(toMergeOrders, mergeParamsMock);
        }
    }

    public class MergePositionCallSetup {

        @Before
        public void setUp() {
            testObservable = mergeTask.mergePosition(instrumentEURUSD, mergeParamsMock);
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

            verify(splitterMock).observe(toMergeOrders, mergeParamsMock);
            verify(positionUtilMock).filledOrders(instrumentEURUSD);
        }
    }

    public class MergeAllCallSetup {

        private List<Observable<OrderEvent>> closeObservables;

        @Before
        public void setUp() throws Exception {
            when(paramsFactoryMock.apply(instrumentEURUSD)).thenReturn(mergeParamsMock);
        }

        private void mergeAllSubscribe() {
            testObserver = mergeTask
                .mergeAll(paramsFactoryMock)
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

            verify(paramsFactoryMock).apply(instrumentEURUSD);
            verify(splitterMock).observe(toMergeOrders, mergeParamsMock);
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
