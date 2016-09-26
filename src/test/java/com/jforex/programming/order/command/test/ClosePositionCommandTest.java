package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.BatchMode;
import com.jforex.programming.order.command.CloseExecutionMode;
import com.jforex.programming.order.command.ClosePositionCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

public class ClosePositionCommandTest extends InstrumentUtilForTest {

    private ClosePositionCommand command;

    @Mock
    private MergeCommand mergeCommandMock;
    private final OrderEvent testEvent = closeEvent;
    private final OrderEvent composerEvent = changedLabelEvent;
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> testComposer =
            obs -> obs.flatMap(orderEvent -> Observable.just(composerEvent));
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> testOrderComposer =
            (obs, order) -> obs.flatMap(orderEvent -> Observable.just(composerEvent));

    private void assertComposerIsNeutral(final Function<Observable<OrderEvent>, Observable<OrderEvent>> composer) {
        final TestObserver<OrderEvent> testObserver = Observable
            .just(testEvent)
            .compose(composer)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(testEvent);
    }

    private void assertComposerEmitsComposerEvent(final Function<Observable<OrderEvent>,
                                                                 Observable<OrderEvent>> composer) {
        final TestObserver<OrderEvent> testObserver = Observable
            .just(testEvent)
            .compose(composer)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(composerEvent);
    }

    @Test
    public void defaultCommandValuesAreCorrect() {
        command = ClosePositionCommand
            .newBuilder(instrumentEURUSD)
            .singleCloseComposer(testOrderComposer)
            .closeOpened(testComposer, BatchMode.MERGE)
            .build();

        assertThat(command.instrument(), equalTo(instrumentEURUSD));
        assertFalse(command.maybeMergeCommand().isPresent());
        assertComposerEmitsComposerEvent(command.singleCloseComposer(buyOrderEURUSD));
        assertComposerIsNeutral(command.closeAllComposer());
        assertComposerIsNeutral(command.closeFilledComposer());
    }

    @Test
    public void definedValuesForCloseFilledAreCorrect() {
        command = ClosePositionCommand
            .newBuilder(instrumentEURUSD)
            .singleCloseComposer(testOrderComposer)
            .closeFilled(testComposer, BatchMode.MERGE)
            .withMergeCommand(mergeCommandMock)
            .build();

        assertThat(command.instrument(), equalTo(instrumentEURUSD));
        assertTrue(command.maybeMergeCommand().isPresent());
        assertThat(command.executionMode(), equalTo(CloseExecutionMode.CloseFilled));
        assertComposerIsNeutral(command.closeAllComposer());
        assertComposerIsNeutral(command.closeOpenedComposer());
        assertComposerEmitsComposerEvent(command.closeFilledComposer());
    }

    @Test
    public void definedValuesForCloseFilledOrOpenedAreCorrect() {
        command = ClosePositionCommand
            .newBuilder(instrumentEURUSD)
            .singleCloseComposer(testOrderComposer)
            .closeAll(testComposer, BatchMode.MERGE)
            .withMergeCommand(mergeCommandMock)
            .build();

        assertThat(command.instrument(), equalTo(instrumentEURUSD));
        assertTrue(command.maybeMergeCommand().isPresent());
        assertThat(command.executionMode(), equalTo(CloseExecutionMode.CloseAll));
        assertComposerIsNeutral(command.closeFilledComposer());
        assertComposerIsNeutral(command.closeOpenedComposer());
        assertComposerEmitsComposerEvent(command.closeAllComposer());
    }

    @Test
    public void definedValuesForCloseOpenedAreCorrect() {
        command = ClosePositionCommand
            .newBuilder(instrumentEURUSD)
            .singleCloseComposer(testOrderComposer)
            .closeOpened(testComposer, BatchMode.MERGE)
            .build();

        assertThat(command.instrument(), equalTo(instrumentEURUSD));
        assertFalse(command.maybeMergeCommand().isPresent());
        assertThat(command.executionMode(), equalTo(CloseExecutionMode.CloseOpened));
        assertComposerIsNeutral(command.closeFilledComposer());
        assertComposerIsNeutral(command.closeAllComposer());
        assertComposerEmitsComposerEvent(command.closeOpenedComposer());
    }
}
