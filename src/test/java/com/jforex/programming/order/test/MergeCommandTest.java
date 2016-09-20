package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.MergeCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class MergeCommandTest extends InstrumentUtilForTest {

    private MergeCommand mergeCommand;

    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private static final String mergeOrderLabel = "mergeOrderLabel";

    @Test
    public void defaultCommandValuesAreCorrect() throws Exception {
        final MergeCommand mergeCommand = MergeCommand
            .newBuilder(mergeOrderLabel, toMergeOrders)
            .withMergeOption()
            .done()
            .
            .build();

        assertThat(commandParent, equalTo(commandParentMock));
    }
}
