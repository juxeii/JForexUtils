package com.jforex.programming.order.task.test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.ClosePositionTask;
import com.jforex.programming.order.task.MergeAndClosePositionTask;
import com.jforex.programming.order.task.params.position.CloseAllPositionsParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class ClosePositionTaskTest extends InstrumentUtilForTest {

    private ClosePositionTask closePositionTask;

    @Mock
    private MergeAndClosePositionTask paramsHandlerMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ClosePositionParams closePositionParamsMock;
    @Mock
    private CloseAllPositionsParams closeAllPositionsParamsMock;
    @Mock
    private Function<Instrument, ClosePositionParams> closePositonParamsFactoryMock;
    @Captor
    private ArgumentCaptor<Function<Instrument, Observable<OrderEvent>>> factoryCaptor;
    private final OrderEvent event = closeEvent;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() throws Exception {
        closePositionTask = new ClosePositionTask(paramsHandlerMock,
                                                  positionUtilMock);
    }

    private void setUpCommandObservables(final Observable<OrderEvent> mergeObservable,
                                         final Observable<OrderEvent> closeObservable) {
        when(paramsHandlerMock.observeMerge(closePositionParamsMock))
            .thenReturn(mergeObservable);
        when(paramsHandlerMock.observeClose(closePositionParamsMock))
            .thenReturn(closeObservable);
    }

    @Test
    public void closeCallIsDeferred() {
        closePositionTask.close(closePositionParamsMock);

        verifyZeroInteractions(paramsHandlerMock);
        verifyZeroInteractions(positionUtilMock);
    }

    @Test
    public void closeAllCallIsDeferred() {
        closePositionTask.closeAll(closeAllPositionsParamsMock);

        verifyZeroInteractions(paramsHandlerMock);
        verifyZeroInteractions(positionUtilMock);
    }

    public class WhenSubscribedToCloseTests {

        private void setUpCommandObservablesAndSubscribe(final Observable<OrderEvent> mergeObservable,
                                                         final Observable<OrderEvent> closeObservable) {
            setUpCommandObservables(mergeObservable, closeObservable);
            testObserver = closePositionTask
                .close(closePositionParamsMock)
                .test();
        }

        @Test
        public void verifyThatCommandHandlerMethodsAreCalled() {
            setUpCommandObservablesAndSubscribe(neverObservable(),
                                                neverObservable());

            verify(paramsHandlerMock).observeMerge(closePositionParamsMock);
            verify(paramsHandlerMock).observeClose(closePositionParamsMock);
        }

        @Test
        public void verifyThatCloseIsConcatenatedWithMerge() {
            setUpCommandObservablesAndSubscribe(neverObservable(),
                                                eventObservable(event));

            testObserver.assertNoValues();
            testObserver.assertNotComplete();
        }

        @Test
        public void closeCompletesWhenMergeAndCloseComplete() {
            setUpCommandObservablesAndSubscribe(emptyObservable(),
                                                emptyObservable());

            testObserver.assertComplete();
        }
    }

    public class WhenSubscribedCloseAllTests {

        private List<Observable<OrderEvent>> closeObservables;

        private void closeAllSubscribe() {
            testObserver = closePositionTask
                .closeAll(closeAllPositionsParamsMock)
                .test();
        }

        private void setUpPositionUtilObservables(final Observable<OrderEvent> firstObservable,
                                                  final Observable<OrderEvent> secondObservable) {
            closeObservables = Stream
                .of(firstObservable, secondObservable)
                .collect(Collectors.toList());
            when(positionUtilMock.observablesFromFactory(factoryCaptor.capture()))
                .thenReturn(closeObservables);

            closeAllSubscribe();
        }

        @Test
        public void verifyThatCloseCommandsAreMerged() throws Exception {
            when(closeAllPositionsParamsMock.closePositonParamsFactory())
                .thenReturn(closePositonParamsFactoryMock);

            setUpPositionUtilObservables(neverObservable(), eventObservable(event));

            testObserver.assertValue(event);
            testObserver.assertNotComplete();

            factoryCaptor.getValue().apply(instrumentEURUSD);
        }

        @Test
        public void closeAllCompletesWhenAllSingleCommandsComplete() {
            setUpPositionUtilObservables(emptyObservable(), emptyObservable());

            testObserver.assertComplete();
        }
    }
}
