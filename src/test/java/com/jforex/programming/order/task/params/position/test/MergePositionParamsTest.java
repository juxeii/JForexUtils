package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class MergePositionParamsTest extends CommonParamsForTest {

    private MergePositionParams mergePositionParams;

    private static final String mergeOrderLabel = "mergeOrderLabel";

    @Test
    public void valuesAreCorrect() {
        mergePositionParams = MergePositionParams
            .newBuilder(instrumentEURUSD, mergeOrderLabel)
            .build();

        assertThat(mergePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(mergePositionParams.mergeOrderLabel(), equalTo(mergeOrderLabel));

        // TODO: add tests for consumerForEvents
    }
}
