package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.event.OrderToEventTransformer;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.CloseParams;
import com.jforex.programming.order.task.params.ClosePositionParams;
import com.jforex.programming.order.task.params.MergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

public class ClosePositionParamsTest extends InstrumentUtilForTest {

    private ClosePositionParams positionParams;

    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private Function<IOrder, CloseParams> closeParamsProviderMock;
    private final OrderEvent testEvent = closeEvent;
    private final OrderEvent composerEvent = changedLabelEvent;
    private final OrderEventTransformer testComposer =
            upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));
    private final OrderToEventTransformer testOrderComposer =
            order -> upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));

    private void assertComposerIsNeutral(final ObservableTransformer<OrderEvent,
                                                                     OrderEvent> composer) {
        final TestObserver<OrderEvent> testObserver = Observable
            .just(testEvent)
            .compose(composer)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(testEvent);
    }

    private void assertComposerEmitsComposerEvent(final ObservableTransformer<OrderEvent,
                                                                              OrderEvent> composer) {
        final TestObserver<OrderEvent> testObserver = Observable
            .just(testEvent)
            .compose(composer)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(composerEvent);
    }

    @Test
    public void defaultParamsValuesAreCorrect() {
        positionParams = ClosePositionParams
            .newBuilder(closeParamsProviderMock)
            .closeOpenedComposer(testComposer, BatchMode.MERGE)
            .build();

        assertThat(positionParams.closeParamsProvider(), equalTo(closeParamsProviderMock));
        assertFalse(positionParams.maybeMergeParams().isPresent());
        assertThat(positionParams.closeBatchMode(), equalTo(BatchMode.MERGE));
        assertComposerIsNeutral(positionParams.singleCloseComposer(buyOrderEURUSD));
        assertComposerIsNeutral(positionParams.singleCloseComposer(buyOrderEURUSD));
        assertComposerIsNeutral(positionParams.closeAllComposer());
        assertComposerIsNeutral(positionParams.closeFilledComposer());
    }

    @Test
    public void definedValuesForCloseFilledAreCorrect() {
        positionParams = ClosePositionParams
            .newBuilder(closeParamsProviderMock)
            .singleCloseComposer(testOrderComposer)
            .closeFilledComposer(testComposer, BatchMode.CONCAT)
            .withMergeParams(mergePositionParamsMock)
            .build();

        assertThat(positionParams.closeParamsProvider(), equalTo(closeParamsProviderMock));
        assertTrue(positionParams.maybeMergeParams().isPresent());
        assertThat(positionParams.closeBatchMode(), equalTo(BatchMode.CONCAT));
        assertThat(positionParams.executionMode(), equalTo(CloseExecutionMode.CloseFilled));
        assertComposerIsNeutral(positionParams.closeAllComposer());
        assertComposerIsNeutral(positionParams.closeOpenedComposer());
        assertComposerEmitsComposerEvent(positionParams.singleCloseComposer(buyOrderEURUSD));
        assertComposerEmitsComposerEvent(positionParams.closeFilledComposer());
    }

    @Test
    public void definedValuesForCloseFilledOrOpenedAreCorrect() {
        positionParams = ClosePositionParams
            .newBuilder(closeParamsProviderMock)
            .singleCloseComposer(testOrderComposer)
            .closeAllComposer(testComposer, BatchMode.MERGE)
            .withMergeParams(mergePositionParamsMock)
            .build();

        assertThat(positionParams.closeParamsProvider(), equalTo(closeParamsProviderMock));
        assertTrue(positionParams.maybeMergeParams().isPresent());
        assertThat(positionParams.executionMode(), equalTo(CloseExecutionMode.CloseAll));
        assertComposerIsNeutral(positionParams.closeFilledComposer());
        assertComposerIsNeutral(positionParams.closeOpenedComposer());
        assertComposerEmitsComposerEvent(positionParams.closeAllComposer());
    }

    @Test
    public void definedValuesForCloseOpenedAreCorrect() {
        positionParams = ClosePositionParams
            .newBuilder(closeParamsProviderMock)
            .singleCloseComposer(testOrderComposer)
            .closeOpenedComposer(testComposer, BatchMode.MERGE)
            .build();

        assertThat(positionParams.closeParamsProvider(), equalTo(closeParamsProviderMock));
        assertFalse(positionParams.maybeMergeParams().isPresent());
        assertThat(positionParams.executionMode(), equalTo(CloseExecutionMode.CloseOpened));
        assertComposerIsNeutral(positionParams.closeFilledComposer());
        assertComposerIsNeutral(positionParams.closeAllComposer());
        assertComposerEmitsComposerEvent(positionParams.closeOpenedComposer());
    }
}
