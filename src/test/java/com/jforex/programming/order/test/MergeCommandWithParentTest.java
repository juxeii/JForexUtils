package com.jforex.programming.order.test;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.CommandParent;
import com.jforex.programming.order.CommonMergeCommand;
import com.jforex.programming.order.CommonMergeCommand.MergeExecutionMode;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class MergeCommandWithParentTest extends InstrumentUtilForTest {

    private CommonMergeCommand mergeCommandWithParent;

    private static final String mergeOrderLabel = "mergeOrderLabel";

    @Mock
    private CommandParent<String> commandParentMock;

    @Test
    public void defaultCommandValuesAreCorrect() throws Exception {
        CommonMergeCommand
            .newBuilder(commandParentMock, mergeOrderLabel)
            .withCancelSLAndTP(obs -> obs)
            .withCancelSL((obs, o) -> obs)
            .withCancelTP((obs, o) -> obs)
            .withExecutionMode(MergeExecutionMode.ConcatSLAndTP)
            .withMerge(obs -> obs)
            .done();

        // assertThat(commandParent, equalTo(commandParentMock));
    }
}
