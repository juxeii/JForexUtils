package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.MergeExecutionMode;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

import io.reactivex.functions.Action;

public class MergePositionParamsTest extends CommonParamsForTest {

    private MergePositionParams mergePositionParams;
    @Mock
    private Action actionMock;
    @Mock
    private Function<IOrder, Action> actionConsumerMock;
    @Mock
    private Consumer<Throwable> errorConsumerMock;
    @Mock
    private BiConsumer<Throwable, IOrder> biErrorConsumerMock;
    @Mock
    private Consumer<OrderEvent> eventConsumerMock;
    private final IOrder orderForTest = buyOrderEURUSD;
    private static final String mergeOrderLabel = "mergeOrderLabel";
    private static final int noOfRetries = 3;
    private static final long delayInMillis = 1500L;

    @Before
    public void setUp() {
        when(actionConsumerMock.apply(orderForTest)).thenReturn(actionMock);

        mergePositionParams = MergePositionParams
            .newBuilder(instrumentEURUSD, mergeOrderLabel)

            .withMergeExecutionMode(MergeExecutionMode.ConcatCancelSLAndTP)
            .withBatchCancelSLMode(BatchMode.CONCAT)
            .withBatchCancelTPMode(BatchMode.CONCAT)

            .doOnMergePositionStart(actionMock)
            .doOnMergePositionComplete(actionMock)
            .doOnMergePositionError(errorConsumerMock)
            .retryOnMergePositionReject(noOfRetries, delayInMillis)

            .doOnCancelSLTPStart(actionMock)
            .doOnCancelSLTPComplete(actionMock)
            .doOnCancelSLTPError(errorConsumerMock)
            .retryOnCancelSLTPReject(noOfRetries, delayInMillis)

            .doOnBatchCancelSLStart(actionMock)
            .doOnBatchCancelSLComplete(actionMock)
            .doOnBatchCancelSLError(errorConsumerMock)
            .retryOnBatchCancelSLReject(noOfRetries, delayInMillis)

            .doOnBatchCancelTPStart(actionMock)
            .doOnBatchCancelTPComplete(actionMock)
            .doOnBatchCancelTPError(errorConsumerMock)
            .retryOnBatchCancelTPReject(noOfRetries, delayInMillis)

            .doOnCancelSLStart(actionConsumerMock)
            .doOnCancelSLComplete(actionConsumerMock)
            .doOnCancelSLError(biErrorConsumerMock)
            .retryOnCancelSLReject(noOfRetries, delayInMillis)

            .doOnCancelTPStart(actionConsumerMock)
            .doOnCancelTPComplete(actionConsumerMock)
            .doOnCancelTPError(biErrorConsumerMock)
            .retryOnCancelTPReject(noOfRetries, delayInMillis)

            .doOnMergeStart(actionMock)
            .doOnMergeComplete(actionMock)
            .doOnMergeError(errorConsumerMock)
            .retryOnMergeReject(noOfRetries, delayInMillis)

            .doOnCancelSL(eventConsumerMock)
            .doOnCancelSLReject(eventConsumerMock)

            .doOnCancelTP(eventConsumerMock)
            .doOnCancelTPReject(eventConsumerMock)

            .doOnMerge(eventConsumerMock)
            .doOnMergeClose(eventConsumerMock)
            .doOnMergeReject(eventConsumerMock)
            .build();
    }

    private void assertComposeData(final ComposeData composeData) {
        assertActions(composeData);
        assertErrorConsumer(composeData.errorConsumer());
        assertRetries(composeData.retryParams());
    }

    private void assertActions(final ComposeData composeData) {
        assertThat(composeData.startAction(), equalTo(actionMock));
        assertThat(composeData.completeAction(), equalTo(actionMock));
    }

    private void assertErrorConsumer(final Consumer<Throwable> errorConsumer) {
        errorConsumer.accept(jfException);

        verify(errorConsumerMock).accept(any());
    }

    private void assertRetries(final RetryParams retryParams) {
        assertThat(retryParams.noOfRetries(), equalTo(noOfRetries));
        assertThat(retryParams.delayInMillis(), equalTo(delayInMillis));
    }

    private void assertEventHandler(final OrderEventType type) {
        assertThat(mergePositionParams.consumerForEvent().get(type), equalTo(eventConsumerMock));
    }

    private void assertComposeDataWithOrder(final ComposeData composeData) throws Exception {
        composeData.startAction().run();
        verify(actionMock).run();

        composeData.completeAction().run();
        verify(actionMock, times(2)).run();

        composeData.errorConsumer().accept(jfException);
        verify(biErrorConsumerMock).accept(jfException, orderForTest);
    }

    @Test
    public void defaultValuesAreCorrect() {
        mergePositionParams = MergePositionParams
            .newBuilder(instrumentEURUSD, mergeOrderLabel)
            .build();

        assertThat(mergePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(mergePositionParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergePositionParams.mergeExecutionMode(), equalTo(MergeExecutionMode.MergeCancelSLAndTP));
        assertThat(mergePositionParams.batchCancelSLMode(), equalTo(BatchMode.MERGE));
        assertThat(mergePositionParams.batchCancelTPMode(), equalTo(BatchMode.MERGE));
        assertThat(mergePositionParams.consumerForEvent().size(), equalTo(0));
    }

    @Test
    public void assertSpecifiedValues() {
        assertThat(mergePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(mergePositionParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergePositionParams.mergeExecutionMode(), equalTo(MergeExecutionMode.ConcatCancelSLAndTP));
        assertThat(mergePositionParams.batchCancelSLMode(), equalTo(BatchMode.CONCAT));
        assertThat(mergePositionParams.batchCancelTPMode(), equalTo(BatchMode.CONCAT));
    }

    @Test
    public void assertMergePositionValues() {
        assertComposeData(mergePositionParams.mergePositionComposeParams());
    }

    @Test
    public void assertCancelSLTPValues() {
        assertComposeData(mergePositionParams.cancelSLTPComposeParams());
    }

    @Test
    public void assertBatchCancelSLValues() {
        assertComposeData(mergePositionParams.batchCancelSLComposeParams());
    }

    @Test
    public void assertBatchCancelTPValues() {
        assertComposeData(mergePositionParams.batchCancelTPComposeParams());
    }

    @Test
    public void assertCancelSLValues() throws Exception {
        final ComposeData composeData = mergePositionParams.cancelSLComposeParams(orderForTest);
        assertComposeDataWithOrder(composeData);
    }

    @Test
    public void assertCancelTPValues() throws Exception {
        final ComposeData composeData = mergePositionParams.cancelTPComposeParams(orderForTest);
        assertComposeDataWithOrder(composeData);
    }

    @Test
    public void assertMergeValues() {
        assertComposeData(mergePositionParams.mergeComposeParams());
    }

    @Test
    public void orderEventHandlersAreCorrect() {
        assertThat(mergePositionParams.consumerForEvent().size(), equalTo(7));
        assertEventHandler(OrderEventType.MERGE_OK);
        assertEventHandler(OrderEventType.MERGE_CLOSE_OK);
        assertEventHandler(OrderEventType.MERGE_REJECTED);
        assertEventHandler(OrderEventType.CHANGED_SL);
        assertEventHandler(OrderEventType.CHANGED_TP);
        assertEventHandler(OrderEventType.CHANGE_SL_REJECTED);
        assertEventHandler(OrderEventType.CHANGE_TP_REJECTED);
    }
}
