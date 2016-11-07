package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.event.OrderToEventTransformer;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.MergeExecutionMode;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.observers.TestObserver;

public class MergeParamsTest extends InstrumentUtilForTest {

    private MergePositionParams mergeParams;

    private static final String mergeOrderLabel = "mergeOrderLabel";
    private final OrderEvent testEvent = mergeEvent;
    private final OrderEvent composerEvent = changedLabelEvent;

    private void assertComposerIsNeutral(final ObservableTransformer<OrderEvent, OrderEvent> composer) {
        final TestObserver<OrderEvent> testObserver = Observable
            .just(testEvent)
            .compose(composer)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(testEvent);
    }

    private void assertComposerEmitsComposerEvent(final ObservableTransformer<OrderEvent, OrderEvent> composer) {
        final TestObserver<OrderEvent> testObserver = Observable
            .just(testEvent)
            .compose(composer)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(composerEvent);
    }

    @Test
    public void defaultParamsValuesAreCorrect() throws Exception {
        mergeParams = MergePositionParams
            .newBuilder(mergeOrderLabel)
            .build();

        assertThat(mergeParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergeParams.executionMode(), equalTo(MergeExecutionMode.MergeCancelSLAndTP));
        assertThat(mergeParams.orderCancelSLMode(), equalTo(BatchMode.MERGE));
        assertThat(mergeParams.orderCancelTPMode(), equalTo(BatchMode.MERGE));
        assertComposerIsNeutral(mergeParams.cancelSLTPComposer());
        assertComposerIsNeutral(mergeParams.cancelSLComposer());
        assertComposerIsNeutral(mergeParams.cancelTPComposer());
        assertComposerIsNeutral(mergeParams.orderCancelSLComposer(buyOrderEURUSD));
        assertComposerIsNeutral(mergeParams.orderCancelTPComposer(buyOrderEURUSD));
        assertComposerIsNeutral(mergeParams.mergeComposer());
    }

    @Test
    public void definedValuesAreCorrect() throws Exception {
        final OrderEventTransformer testComposer =
                upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));
        final OrderToEventTransformer testOrderComposer =
                order -> upstream -> upstream
                    .flatMap(orderEvent -> Observable.just(composerEvent));

        mergeParams = MergePositionParams
            .newBuilder(mergeOrderLabel)
            .composeCancelSLAndTP(testComposer)
            .composeCancelSL(testComposer)
            .composeCancelTP(testComposer)
            .composeOrderCancelSL(testOrderComposer, BatchMode.MERGE)
            .composeOrderCancelTP(testOrderComposer, BatchMode.CONCAT)
            .withExecutionMode(MergeExecutionMode.ConcatCancelSLAndTP)
            .done()
            .composeMerge(testComposer)
            .build();

        assertThat(mergeParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergeParams.executionMode(), equalTo(MergeExecutionMode.ConcatCancelSLAndTP));
        assertThat(mergeParams.orderCancelSLMode(), equalTo(BatchMode.MERGE));
        assertThat(mergeParams.orderCancelTPMode(), equalTo(BatchMode.CONCAT));
        assertComposerEmitsComposerEvent(mergeParams.cancelSLTPComposer());
        assertComposerEmitsComposerEvent(mergeParams.cancelSLComposer());
        assertComposerEmitsComposerEvent(mergeParams.cancelTPComposer());
        assertComposerEmitsComposerEvent(mergeParams.orderCancelSLComposer(buyOrderEURUSD));
        assertComposerEmitsComposerEvent(mergeParams.orderCancelTPComposer(buyOrderEURUSD));
        assertComposerEmitsComposerEvent(mergeParams.mergeComposer());
    }
}
