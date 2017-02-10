package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
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
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

import io.reactivex.functions.Action;

public class MergePositionParamsTest extends CommonParamsForTest {

    private MergePositionParams mergePositionParams;

    @Mock
    private TaskParamsBase mergePositionComposeParamsMock;
    @Mock
    private TaskParamsBase cancelSLTPComposeParamsMock;
    @Mock
    private TaskParamsBase batchCancelSLComposeParamsMock;
    @Mock
    private TaskParamsBase batchCancelTPComposeParamsMock;
    @Mock
    private Function<IOrder, TaskParamsBase> cancelSLComposeParamsMock;
    @Mock
    private TaskParamsBase cancelSLComposeMock;
    @Mock
    private Function<IOrder, TaskParamsBase> cancelTPComposeParamsMock;
    @Mock
    private TaskParamsBase cancelTPComposeMock;
    @Mock
    private TaskParamsBase mergeComposeParamsMock;
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
    private final ComposeData composeData = new ComposeParams();
    private final IOrder orderForTest = buyOrderEURUSD;
    private static final String mergeOrderLabel = "mergeOrderLabel";

    @Before
    public void setUp() {
        when(actionConsumerMock.apply(orderForTest)).thenReturn(actionMock);

        when(mergePositionComposeParamsMock.composeData()).thenReturn(composeData);
        when(cancelSLTPComposeParamsMock.composeData()).thenReturn(composeData);
        when(batchCancelSLComposeParamsMock.composeData()).thenReturn(composeData);
        when(batchCancelTPComposeParamsMock.composeData()).thenReturn(composeData);
        when(mergeComposeParamsMock.composeData()).thenReturn(composeData);

        when(cancelSLComposeParamsMock.apply(orderForTest)).thenReturn(cancelSLComposeMock);
        when(cancelTPComposeParamsMock.apply(orderForTest)).thenReturn(cancelTPComposeMock);
        when(cancelSLComposeMock.composeData()).thenReturn(composeData);
        when(cancelTPComposeMock.composeData()).thenReturn(composeData);

        mergePositionParams = MergePositionParams
            .newBuilder(instrumentEURUSD, mergeOrderLabel)

            .withMergeExecutionMode(CancelSLTPMode.ConcatCancelSLAndTP)
            .withBatchCancelSLMode(BatchMode.CONCAT)
            .withBatchCancelTPMode(BatchMode.CONCAT)

            .withMergePositonParams(mergePositionComposeParamsMock)
            .withCancelSLTPParams(cancelSLTPComposeParamsMock)
            .withBatchCancelSLParams(batchCancelSLComposeParamsMock)
            .withBatchCancelTPParams(batchCancelTPComposeParamsMock)
            .withCancelSLParams(cancelSLComposeParamsMock)
            .withCancelTPParams(cancelTPComposeParamsMock)
            .withMergeParams(mergeComposeParamsMock)

            .doOnCancelSL(eventConsumerMock)
            .doOnCancelSLReject(eventConsumerMock)

            .doOnCancelTP(eventConsumerMock)
            .doOnCancelTPReject(eventConsumerMock)

            .doOnMerge(eventConsumerMock)
            .doOnMergeClose(eventConsumerMock)
            .doOnMergeReject(eventConsumerMock)

            .build();
    }

    private void assertEventHandler(final OrderEventType type) {
        assertThat(mergePositionParams.consumerForEvent().get(type), equalTo(eventConsumerMock));
    }

    @Test
    public void defaultValuesAreCorrect() {
        mergePositionParams = MergePositionParams
            .newBuilder(instrumentEURUSD, mergeOrderLabel)
            .build();

        assertThat(mergePositionParams.type(), equalTo(TaskParamsType.MERGEPOSITION));
        assertThat(mergePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(mergePositionParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergePositionParams.mergeExecutionMode(), equalTo(CancelSLTPMode.MergeCancelSLAndTP));
        assertThat(mergePositionParams.batchCancelSLMode(), equalTo(BatchMode.MERGE));
        assertThat(mergePositionParams.batchCancelTPMode(), equalTo(BatchMode.MERGE));
        assertThat(mergePositionParams.consumerForEvent().size(), equalTo(0));

        assertNotNull(mergePositionParams.cancelSLComposeParams(orderForTest));
        assertNotNull(mergePositionParams.cancelTPComposeParams(orderForTest));
    }

    @Test
    public void assertSpecifiedValues() {
        assertThat(mergePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(mergePositionParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergePositionParams.mergeExecutionMode(), equalTo(CancelSLTPMode.ConcatCancelSLAndTP));
        assertThat(mergePositionParams.batchCancelSLMode(), equalTo(BatchMode.CONCAT));
        assertThat(mergePositionParams.batchCancelTPMode(), equalTo(BatchMode.CONCAT));
    }

    @Test
    public void assertMergePositionValues() {
        assertThat(mergePositionParams.composeData(), equalTo(composeData));
    }

    @Test
    public void assertCancelSLTPValues() {
        assertThat(mergePositionParams.cancelSLTPComposeParams(), equalTo(composeData));
    }

    @Test
    public void assertBatchCancelSLValues() {
        assertThat(mergePositionParams.batchCancelSLComposeParams(), equalTo(composeData));
    }

    @Test
    public void assertBatchCancelTPValues() {
        assertThat(mergePositionParams.batchCancelTPComposeParams(), equalTo(composeData));
    }

    @Test
    public void assertCancelSLValues() throws Exception {
        assertThat(mergePositionParams.cancelSLComposeParams(orderForTest), equalTo(composeData));
    }

    @Test
    public void assertCancelTPValues() throws Exception {
        assertThat(mergePositionParams.cancelTPComposeParams(orderForTest), equalTo(composeData));
    }

    @Test
    public void assertMergeValues() {
        assertThat(mergePositionParams.mergeComposeParams(), equalTo(composeData));
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
