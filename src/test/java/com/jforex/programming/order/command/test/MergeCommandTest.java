package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergeExecutionMode;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

public class MergeCommandTest extends InstrumentUtilForTest {

    private MergeCommand mergeCommand;

    private static final String mergeOrderLabel = "mergeOrderLabel";
    private final OrderEvent testEvent = mergeEvent;
    private final OrderEvent composerEvent = changedLabelEvent;

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
    public void defaultCommandValuesAreCorrect() throws Exception {
        mergeCommand = MergeCommand
            .newBuilder(mergeOrderLabel)
            .build();

        assertThat(mergeCommand.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergeCommand.executionMode(), equalTo(MergeExecutionMode.MergeCancelSLAndTP));
        assertComposerIsNeutral(mergeCommand.cancelSLTPCompose());
        assertComposerIsNeutral(mergeCommand.cancelSLCompose());
        assertComposerIsNeutral(mergeCommand.cancelTPCompose());
        assertComposerIsNeutral(mergeCommand.orderCancelSLComposer(buyOrderEURUSD));
        assertComposerIsNeutral(mergeCommand.orderCancelTPComposer(buyOrderEURUSD));
        assertComposerIsNeutral(mergeCommand.mergeCompose());
    }

    @Test
    public void definedValuesAreCorrect() throws Exception {
        final Function<Observable<OrderEvent>, Observable<OrderEvent>> testComposer =
                obs -> obs.flatMap(orderEvent -> Observable.just(composerEvent));
        final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> testOrderComposer =
                (obs, order) -> obs.flatMap(orderEvent -> Observable.just(composerEvent));

        mergeCommand = MergeCommand
            .newBuilder(mergeOrderLabel)
            .composeCancelSLAndTP(testComposer)
            .composeCancelSL(testComposer)
            .composeCancelTP(testComposer)
            .composeOrderCancelSL(testOrderComposer)
            .composeOrderCancelTP(testOrderComposer)
            .withExecutionMode(MergeExecutionMode.ConcatCancelSLAndTP)
            .done()
            .composeMerge(testComposer)
            .build();

        assertThat(mergeCommand.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergeCommand.executionMode(), equalTo(MergeExecutionMode.ConcatCancelSLAndTP));
        assertComposerEmitsComposerEvent(mergeCommand.cancelSLTPCompose());
        assertComposerEmitsComposerEvent(mergeCommand.cancelSLCompose());
        assertComposerEmitsComposerEvent(mergeCommand.cancelTPCompose());
        assertComposerEmitsComposerEvent(mergeCommand.orderCancelSLComposer(buyOrderEURUSD));
        assertComposerEmitsComposerEvent(mergeCommand.orderCancelTPComposer(buyOrderEURUSD));
        assertComposerEmitsComposerEvent(mergeCommand.mergeCompose());
    }
}
