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
import com.jforex.programming.order.task.CloseTask;
import com.jforex.programming.order.task.params.ClosePositionParams;
import com.jforex.programming.order.task.params.ClosePositionParamsHandler;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CloseTaskTest extends InstrumentUtilForTest {

    private CloseTask closeTask;

    @Mock
    private ClosePositionParamsHandler commandHandlerMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ClosePositionParams closePositionCommandMock;
    @Mock
    private Function<Instrument, ClosePositionParams> commandFactoryMock;
    private final OrderEvent event = closeEvent;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() throws Exception {
        closeTask = new CloseTask(commandHandlerMock, positionUtilMock);
    }

    private void setUpCommandObservables(final Observable<OrderEvent> mergeObservable,
                                         final Observable<OrderEvent> closeObservable) {
        when(commandHandlerMock.observeMerge(closePositionCommandMock))
            .thenReturn(mergeObservable);
        when(commandHandlerMock.observeClose(closePositionCommandMock))
            .thenReturn(closeObservable);
    }

    @Test
    public void closeCallIsDeferred() {
        closeTask.close(closePositionCommandMock);

        verifyZeroInteractions(commandHandlerMock);
        verifyZeroInteractions(positionUtilMock);
    }

    @Test
    public void closeAllCallIsDeferred() {
        closeTask.closeAllPositions(commandFactoryMock);

        verifyZeroInteractions(commandHandlerMock);
        verifyZeroInteractions(positionUtilMock);
    }

    public class WhenSubscribedToCloseTests {

        private void setUpCommandObservablesAndSubscribe(final Observable<OrderEvent> mergeObservable,
                                                         final Observable<OrderEvent> closeObservable) {
            setUpCommandObservables(mergeObservable, closeObservable);
            testObserver = closeTask
                .close(closePositionCommandMock)
                .test();
        }

        @Test
        public void verifyThatCommandHandlerMethodsAreCalled() {
            setUpCommandObservablesAndSubscribe(neverObservable(), neverObservable());

            verify(commandHandlerMock).observeMerge(closePositionCommandMock);
            verify(commandHandlerMock).observeClose(closePositionCommandMock);
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
            when(commandFactoryMock.apply(instrumentEURUSD)).thenReturn(closePositionCommandMock);
        }

        private void closeAllSubscribe() {
            testObserver = closeTask
                .closeAllPositions(commandFactoryMock)
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

            verify(commandFactoryMock).apply(instrumentEURUSD);
            verify(commandHandlerMock).observeMerge(closePositionCommandMock);
            verify(commandHandlerMock).observeClose(closePositionCommandMock);
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
