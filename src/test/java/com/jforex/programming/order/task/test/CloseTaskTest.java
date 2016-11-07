package com.jforex.programming.order.task.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.ClosePositionTask;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.ClosePositionParamsHandler;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CloseTaskTest extends InstrumentUtilForTest {

    private ClosePositionTask closeTask;

    @Mock
    private ClosePositionParamsHandler paramsHandlerMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ClosePositionParams closePositionParamsMock;
    @Mock
    private Function<Instrument, ClosePositionParams> paramsFactoryMock;
    private final OrderEvent event = closeEvent;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() throws Exception {
        closeTask = new ClosePositionTask(paramsHandlerMock, positionUtilMock);
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
        closeTask.close(closePositionParamsMock);

        verifyZeroInteractions(paramsHandlerMock);
        verifyZeroInteractions(positionUtilMock);
    }

    @Test
    public void closeAllCallIsDeferred() {
        closeTask.closeAll(paramsFactoryMock);

        verifyZeroInteractions(paramsHandlerMock);
        verifyZeroInteractions(positionUtilMock);
    }

    public class WhenSubscribedToCloseTests {

        private void setUpCommandObservablesAndSubscribe(final Observable<OrderEvent> mergeObservable,
                                                         final Observable<OrderEvent> closeObservable) {
            setUpCommandObservables(mergeObservable, closeObservable);
            testObserver = closeTask
                .close(closePositionParamsMock)
                .test();
        }

        @Test
        public void verifyThatCommandHandlerMethodsAreCalled() {
            setUpCommandObservablesAndSubscribe(neverObservable(), neverObservable());

            verify(paramsHandlerMock).observeMerge(closePositionParamsMock);
            verify(paramsHandlerMock).observeClose(closePositionParamsMock);
        }

        @Test
        public void verifyThatCloseIsConcatenatedWithMerge() {
            setUpCommandObservablesAndSubscribe(neverObservable(), eventObservable(event));

            testObserver.assertNoValues();
            testObserver.assertNotComplete();
        }

        @Test
        public void closeCompletesWhenMergeAndCloseComplete() {
            setUpCommandObservablesAndSubscribe(emptyObservable(), emptyObservable());

            testObserver.assertComplete();
        }
    }

    public class WhenSubscribedCloseAllTests {

        private List<Observable<OrderEvent>> closeObservables;

        @Before
        public void setUp() throws Exception {
            when(paramsFactoryMock.apply(instrumentEURUSD)).thenReturn(closePositionParamsMock);
        }

        private void closeAllSubscribe() {
            testObserver = closeTask
                .closeAll(paramsFactoryMock)
                .test();
        }

        private void setUpPositionUtilObservables(final Observable<OrderEvent> firstObservable,
                                                  final Observable<OrderEvent> secondObservable) {
            closeObservables = Stream
                .of(firstObservable, secondObservable)
                .collect(Collectors.toList());
            when(positionUtilMock.observablesFromFactory(any())).thenReturn(closeObservables);

            closeAllSubscribe();
        }

        @SuppressWarnings("unchecked")
        @Test
        public void verifyThatPositionUtilIsCalledWithCorrectFactory() throws Exception {
            doAnswer(invocation -> ((Function<Instrument, Observable<OrderEvent>>) invocation.getArgument(0))
                .apply(instrumentEURUSD)
                .subscribe())
                    .when(positionUtilMock).observablesFromFactory(any());

            setUpCommandObservables(emptyObservable(), emptyObservable());
            closeAllSubscribe();

            verify(paramsFactoryMock).apply(instrumentEURUSD);
            verify(paramsHandlerMock).observeMerge(closePositionParamsMock);
            verify(paramsHandlerMock).observeClose(closePositionParamsMock);
        }

        @Test
        public void verifyThatCloseCommandsAreMerged() {
            setUpPositionUtilObservables(neverObservable(), eventObservable(event));

            testObserver.assertValue(event);
            testObserver.assertNotComplete();
        }

        @Test
        public void closeAllCompletesWhenAllSingleCommandsComplete() {
            setUpPositionUtilObservables(emptyObservable(), emptyObservable());

            testObserver.assertComplete();
        }
    }
}
