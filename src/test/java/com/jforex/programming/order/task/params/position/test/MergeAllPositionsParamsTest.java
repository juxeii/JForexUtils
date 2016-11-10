package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.params.position.MergeAllPositionsParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class MergeAllPositionsParamsTest extends CommonParamsForTest {

    private MergeAllPositionsParams mergeAllPositionsParams;

    @Mock
    public MergePositionParams mergePositionParamsMock;

    @Test
    public void valuesAreCorrect() {
        mergeAllPositionsParams = MergeAllPositionsParams
            .withMergePositionParams(instrument -> mergePositionParamsMock)
            .build();

        assertThat(mergeAllPositionsParams.mergePositionParams(instrumentEURUSD),
                   equalTo(mergePositionParamsMock));
    }
}
