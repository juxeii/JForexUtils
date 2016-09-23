package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.MergeCommand;
import com.jforex.programming.order.MergeOrdersCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class MergeOrdersCommandTest extends InstrumentUtilForTest {

    @Mock
    private MergeCommand mergeCommandMock;

    @Test
    public void accessorsAreCorrect() {
        final Collection<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

        final MergeOrdersCommand command = new MergeOrdersCommand(toMergeOrders, mergeCommandMock);

        assertThat(command.toMergeOrders(), equalTo(toMergeOrders));
        assertThat(command.mergeCommand(), equalTo(mergeCommandMock));
    }
}
