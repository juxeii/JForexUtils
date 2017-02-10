package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.ComposeDataImpl;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.basic.CancelSLParams;
import com.jforex.programming.order.task.params.basic.CancelTPParams;
import com.jforex.programming.order.task.params.basic.MergeParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

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
    private CancelSLParams cancelSLParamsMock;
    @Mock
    private Function<IOrder, TaskParamsBase> cancelTPComposeParamsMock;
    @Mock
    private CancelTPParams cancelTPParamsMock;
    @Mock
    private MergeParams mergeParamsMock;
    private final ComposeData composeData = new ComposeDataImpl();

    @Before
    public void setUp() {
        when(mergePositionComposeParamsMock.composeData()).thenReturn(composeData);
        when(cancelSLTPComposeParamsMock.composeData()).thenReturn(composeData);
        when(batchCancelSLComposeParamsMock.composeData()).thenReturn(composeData);
        when(batchCancelTPComposeParamsMock.composeData()).thenReturn(composeData);
        when(mergeParamsMock.composeData()).thenReturn(composeData);

        when(cancelSLComposeParamsMock.apply(orderForTest)).thenReturn(cancelSLParamsMock);
        when(cancelTPComposeParamsMock.apply(orderForTest)).thenReturn(cancelTPParamsMock);
        when(cancelSLParamsMock.composeData()).thenReturn(composeData);
        when(cancelTPParamsMock.composeData()).thenReturn(composeData);

        mergePositionParams = MergePositionParams
            .newBuilder(instrumentEURUSD, mergeOrderLabel)

            .withMergeExecutionMode(CancelSLTPMode.ConcatCancelSLAndTP)
            .withBatchCancelSLMode(BatchMode.CONCAT)
            .withBatchCancelTPMode(BatchMode.CONCAT)

            .withCancelSLTPParams(cancelSLTPComposeParamsMock)
            .withBatchCancelSLParams(batchCancelSLComposeParamsMock)
            .withBatchCancelTPParams(batchCancelTPComposeParamsMock)
            .withCancelSLParams(cancelSLComposeParamsMock)
            .withCancelTPParams(cancelTPComposeParamsMock)
            .withMergeParams(mergeParamsMock)

            .build();
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

        assertNotNull(mergePositionParams.createCancelSLComposeData(orderForTest));
        assertNotNull(mergePositionParams.createCancelTPComposeData(orderForTest));
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
    public void assertCancelSLTPValues() {
        assertThat(mergePositionParams.cancelSLTPComposeData(), equalTo(composeData));
    }

    @Test
    public void assertBatchCancelSLValues() {
        assertThat(mergePositionParams.batchCancelSLComposeData(), equalTo(composeData));
    }

    @Test
    public void assertBatchCancelTPValues() {
        assertThat(mergePositionParams.batchCancelTPComposeData(), equalTo(composeData));
    }

    @Test
    public void assertCancelSLValues() throws Exception {
        assertThat(mergePositionParams.createCancelSLComposeData(orderForTest), equalTo(composeData));
    }

    @Test
    public void assertCancelTPValues() throws Exception {
        assertThat(mergePositionParams.createCancelTPComposeData(orderForTest), equalTo(composeData));
    }

    @Test
    public void assertMergeValues() {
        assertThat(mergePositionParams.mergeComposeData(), equalTo(composeData));
    }
}
