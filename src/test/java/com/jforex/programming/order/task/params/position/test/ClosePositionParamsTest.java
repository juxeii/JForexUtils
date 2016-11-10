package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.position.SimpleClosePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class ClosePositionParamsTest extends CommonParamsForTest {

    private ClosePositionParams closePositionParams;

    @Mock
    private SimpleClosePositionParams simpleClosePositionParamsMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;

    @Test
    public void defaultValuesAreCorrect() {
        closePositionParams = ClosePositionParams
            .newBuilder(instrumentEURUSD, mergePositionParamsMock)
            .build();

        assertThat(closePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseAll));
        assertNotNull(closePositionParams.mergePositionParams());
        assertNotNull(closePositionParams.simpleClosePositionParams());
    }

    @Test
    public void vluesAreCorrect() {
        closePositionParams = ClosePositionParams
            .newBuilder(instrumentEURUSD, mergePositionParamsMock)
            .withCloseExecutionMode(CloseExecutionMode.CloseFilled)
            .withClosePositionParams(simpleClosePositionParamsMock)
            .build();

        assertThat(closePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseFilled));
        assertThat(closePositionParams.simpleClosePositionParams(), equalTo(simpleClosePositionParamsMock));
        assertThat(closePositionParams.mergePositionParams(), equalTo(mergePositionParamsMock));

        // TODO: add tests for consumerForEvents
    }
}
