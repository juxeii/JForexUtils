package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.params.position.BatchCancelSLTPParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.position.SimpleMergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class MergePositionParamsTest extends CommonParamsForTest {

    private MergePositionParams mergePositionParams;

    @Mock
    private BatchCancelSLTPParams batchCancelSLTPParamsMock;
    @Mock
    private SimpleMergePositionParams simpleMergePositionParamsMock;

    @Test
    public void defaultValuesAreCorrect() {
        mergePositionParams = MergePositionParams
            .newBuilder()
            .build();

        assertNotNull(mergePositionParams.batchCancelSLTPParams());
        assertNotNull(mergePositionParams.simpleMergePositionParams());
    }

    @Test
    public void vluesAreCorrect() {
        mergePositionParams = MergePositionParams
            .newBuilder()
            .withBatchCancelSLTPParams(batchCancelSLTPParamsMock)
            .withSimpleMergePositionParams(simpleMergePositionParamsMock)
            .build();

        assertThat(mergePositionParams.simpleMergePositionParams(), equalTo(simpleMergePositionParamsMock));
        assertThat(mergePositionParams.batchCancelSLTPParams(), equalTo(batchCancelSLTPParamsMock));

        // TODO: add tests for consumerForEvents
    }
}
