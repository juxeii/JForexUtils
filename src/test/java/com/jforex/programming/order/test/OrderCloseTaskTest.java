package com.jforex.programming.order.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.ClosePositionCommand;
import com.jforex.programming.order.ClosePositionCommandHandler;
import com.jforex.programming.order.OrderCloseTask;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OrderCloseTaskTest extends InstrumentUtilForTest {

    private OrderCloseTask orderCloseTask;

    @Mock
    private ClosePositionCommandHandler commandHandlerMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ClosePositionCommand closePositionCommandMock;
    @Mock
    private Function<Instrument, ClosePositionCommand> commandFactoryMock;
    private final OrderEvent event = new OrderEvent(buyOrderEURUSD,
                                                    OrderEventType.CLOSE_OK,
                                                    true);
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() throws Exception {
        orderCloseTask = new OrderCloseTask(commandHandlerMock, positionUtilMock);
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
        orderCloseTask.close(closePositionCommandMock);

        verifyZeroInteractions(commandHandlerMock);
        verifyZeroInteractions(positionUtilMock);
    }

    @Test
    public void closeAllCallIsDeferred() {
        orderCloseTask.closeAll(commandFactoryMock);

        verifyZeroInteractions(commandHandlerMock);
        verifyZeroInteractions(positionUtilMock);
    }

    public class WhenSubscribedToCloseTests {

        private void setUpCommandObservablesAndSubscribe(final Observable<OrderEvent> mergeObservable,
                                                         final Observable<OrderEvent> closeObservable) {
            setUpCommandObservables(mergeObservable, closeObservable);
            testObserver = orderCloseTask
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
            testObserver = orderCloseTask
                .closeAll(commandFactoryMock)
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

        @Test
        public void verifyThatPositionUtilIsCalledWithCorrectFactory() throws Exception {
            setUpCommandObservables(emptyObservable(), emptyObservable());
            closeAllSubscribe();

            verify(positionUtilMock)
                .observablesFromFactory(argThat(factory -> {
                    try {
                        factory
                            .apply(instrumentEURUSD)
                            .subscribe();
                    } catch (final Exception e) {
                    }
                    return true;
                }));

            verify(commandFactoryMock, times(2)).apply(instrumentEURUSD);
            verify(commandHandlerMock, times(2)).observeMerge(closePositionCommandMock);
            verify(commandHandlerMock, times(2)).observeClose(closePositionCommandMock);
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
