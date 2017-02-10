package com.jforex.programming.order.task.test;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.CancelSLTPAndMergeTask;
import com.jforex.programming.order.task.MergePositionTask;
import com.jforex.programming.order.task.params.position.MergeAllPositionsParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class MergePositionTaskTest extends InstrumentUtilForTest {

    private MergePositionTask mergeTask;

    @Mock
    private CancelSLTPAndMergeTask splitterMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private MergeAllPositionsParams mergeAllPositionsParamsMock;
    @Mock
    private Function<Instrument, MergePositionParams> mergePositionParamsFactoryMock;
    @Captor
    private ArgumentCaptor<Function<Instrument, Observable<OrderEvent>>> factoryCaptor;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final OrderEvent testEvent = mergeEvent;
    private Observable<OrderEvent> testObservable;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() {
        setUpMocks();

        mergeTask = new MergePositionTask(splitterMock, positionUtilMock);
    }

    private void setUpMocks() {
        when(positionUtilMock.filledOrders(instrumentEURUSD))
            .thenReturn(toMergeOrders);
        when(mergePositionParamsMock.instrument())
            .thenReturn(instrumentEURUSD);
    }

    private void setUpSplitterObservable(final Observable<OrderEvent> splitterObservable) {
        when(splitterMock.observe(toMergeOrders, mergePositionParamsMock))
            .thenReturn(splitterObservable);
    }

    @Test
    public void mergeCallWithNoOrdersForMergeReturnsEmptyObservable() {
        final Set<IOrder> toMergeOrders = Sets.newHashSet();
        when(positionUtilMock.filledOrders(instrumentEURUSD))
            .thenReturn(toMergeOrders);

        mergeTask
            .merge(toMergeOrders, mergePositionParamsMock)
            .test()
            .assertComplete();

        mergeTask
            .merge(mergePositionParamsMock)
            .test()
            .assertComplete();

        verifyZeroInteractions(splitterMock);
    }

    @Test
    public void mergeCallWithOneOrdersForMergeReturnsEmptyObservable() {
        final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD);
        when(positionUtilMock.filledOrders(instrumentEURUSD))
            .thenReturn(toMergeOrders);

        mergeTask
            .merge(toMergeOrders, mergePositionParamsMock)
            .test()
            .assertComplete();

        mergeTask
            .merge(mergePositionParamsMock)
            .test()
            .assertComplete();

        verifyZeroInteractions(splitterMock);
    }

    public class MergeCallSetup {

        @Before
        public void setUp() {
            testObservable = mergeTask.merge(toMergeOrders, mergePositionParamsMock);
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

            verify(splitterMock).observe(toMergeOrders, mergePositionParamsMock);
        }
    }

    public class MergePositionCallSetup {

        @Before
        public void setUp() {
            testObservable = mergeTask.merge(mergePositionParamsMock);
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

            verify(splitterMock).observe(toMergeOrders, mergePositionParamsMock);
            verify(positionUtilMock).filledOrders(instrumentEURUSD);
        }
    }

    public class MergeAllCallSetup {

        private List<Observable<OrderEvent>> closeObservables;

        private void mergeAllSubscribe() {
            testObserver = mergeTask
                .mergeAll(mergeAllPositionsParamsMock)
                .test();
        }

        private void setUpPositionUtilObservables(final Observable<OrderEvent> firstObservable,
                                                  final Observable<OrderEvent> secondObservable) {
            closeObservables = Stream
                .of(firstObservable, secondObservable)
                .collect(Collectors.toList());

            when(positionUtilMock.observablesFromFactory(factoryCaptor.capture()))
                .thenReturn(closeObservables);

            mergeAllSubscribe();
        }

        @Test
        public void mergePositionCallIsDeferred() {
            verifyZeroInteractions(splitterMock);
            verifyZeroInteractions(positionUtilMock);
        }

        @Test
        public void verifyThatMergeCommandsAreMerged() {
            when(mergeAllPositionsParamsMock.mergePositionParamsFactory())
                .thenReturn(mergePositionParamsFactoryMock);

            setUpPositionUtilObservables(neverObservable(),
                                         eventObservable(testEvent));

            testObserver.assertValue(testEvent);
            testObserver.assertNotComplete();

            factoryCaptor.getValue().apply(instrumentEURUSD);
        }

        @Test
        public void completesWhenAllSingleCommandsComplete() {
            setUpPositionUtilObservables(emptyObservable(), emptyObservable());

            testObserver.assertComplete();
        }
    }
}
