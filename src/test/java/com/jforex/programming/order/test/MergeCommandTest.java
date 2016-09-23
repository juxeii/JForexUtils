package com.jforex.programming.order.test;

import java.util.Set;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.MergeOrdersCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class MergeCommandTest extends InstrumentUtilForTest {

    private MergeOrdersCommand mergeCommand;

    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private static final String mergeOrderLabel = "mergeOrderLabel";

    @Test
    public void defaultCommandValuesAreCorrect() throws Exception {
//        final MergeCommand mergeCommand = MergeCommand
//            .newBuilder(mergeOrderLabel, toMergeOrders)
//            .withMergeOption()
//            .withCancelSLAndTP(obs -> obs)
//            .withCancelSL((obs, o) -> obs)
//            .withCancelTP((obs, o) -> obs)
//            .withExecutionMode(MergeExecutionMode.ConcatSLAndTP)
//            .withMerge(obs -> obs)
//            .done()
//            .build();

        // assertThat(commandParent, equalTo(commandParentMock));
    }
}
