package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class ClosePositionParamsTest extends CommonParamsForTest {

    private ClosePositionParams closePositionParams;

    @Mock
    private CloseParams closeParamsMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;

    @Test
    public void defaultValuesAreCorrect() {
        closePositionParams = ClosePositionParams
            .newBuilder(mergePositionParamsMock)
            .build();

        assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseAll));
        assertNotNull(closePositionParams.mergePositionParams());
    }

    @Test
    public void vluesAreCorrect() {
        closePositionParams = ClosePositionParams
            .newBuilder(mergePositionParamsMock)
            .withCloseExecutionMode(CloseExecutionMode.CloseFilled)
            .build();

        assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseFilled));
        assertThat(closePositionParams.mergePositionParams(), equalTo(mergePositionParamsMock));

        // TODO: add tests for consumerForEvents
    }
}
