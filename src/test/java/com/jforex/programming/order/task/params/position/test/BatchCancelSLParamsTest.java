package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.params.position.BatchCancelSLParams;
import com.jforex.programming.order.task.params.position.CancelSLParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class BatchCancelSLParamsTest extends CommonParamsForTest {

    private BatchCancelSLParams batchCancelSLParams;

    @Mock
    public CancelSLParams cancelSLParamsMock;

    @Before
    public void setUp() {
        when(cancelSLParamsMock.consumerForEvent()).thenReturn(consumerForEvent);
    }

    @Test
    public void defaultValuesAreCorrect() {
        batchCancelSLParams = BatchCancelSLParams
            .newBuilder()
            .build();

        assertThat(batchCancelSLParams.batchMode(), equalTo(BatchMode.MERGE));
        assertNotNull(batchCancelSLParams.cancelSLParams());
    }

    @Test
    public void valuesAreCorrect() {
        batchCancelSLParams = BatchCancelSLParams
            .newBuilder()
            .withBatchMode(BatchMode.CONCAT)
            .withCancelSLParams(cancelSLParamsMock)
            .build();

        assertThat(batchCancelSLParams.batchMode(), equalTo(BatchMode.CONCAT));
        assertThat(batchCancelSLParams.consumerForEvent(), equalTo(consumerForEvent));
    }
}
