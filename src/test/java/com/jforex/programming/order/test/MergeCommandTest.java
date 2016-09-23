package com.jforex.programming.order.test;

import org.junit.Test;

import com.jforex.programming.order.MergeCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class MergeCommandTest extends InstrumentUtilForTest {

    private MergeCommand mergeCommandWithParent;

    private static final String mergeOrderLabel = "mergeOrderLabel";

    @Test
    public void defaultCommandValuesAreCorrect() throws Exception {
//        CommonMergeCommand
//            .newBuilder(commandParentMock, mergeOrderLabel)
//            .withCancelSLAndTP(obs -> obs)
//            .withCancelSL((obs, o) -> obs)
//            .withCancelTP((obs, o) -> obs)
//            .withExecutionMode(MergeExecutionMode.ConcatSLAndTP)
//            .withMerge(obs -> obs)
//            .done();

        // assertThat(commandParent, equalTo(commandParentMock));
    }
}
