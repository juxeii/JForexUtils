package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.params.position.BatchCancelTPParams;
import com.jforex.programming.order.task.params.position.CancelTPParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class BatchCancelTPParamsTest extends CommonParamsForTest {

    private BatchCancelTPParams batchCancelTPParams;

    @Mock
    public CancelTPParams cancelTPParamsMock;

    @Before
    public void setUp() {
        when(cancelTPParamsMock.consumerForEvent()).thenReturn(consumerForEvent);
    }

    @Test
    public void defaultValuesAreCorrect() {
        batchCancelTPParams = BatchCancelTPParams
            .newBuilder()
            .build();

        assertThat(batchCancelTPParams.batchMode(), equalTo(BatchMode.MERGE));
        assertNotNull(batchCancelTPParams.cancelTPParams());
    }

    @Test
    public void valuesAreCorrect() {
        batchCancelTPParams = BatchCancelTPParams
            .newBuilder()
            .withBatchMode(BatchMode.CONCAT)
            .withCancelTPParams(cancelTPParamsMock)
            .build();

        assertThat(batchCancelTPParams.batchMode(), equalTo(BatchMode.CONCAT));
        assertThat(batchCancelTPParams.consumerForEvent(), equalTo(consumerForEvent));
    }
}
