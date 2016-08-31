package com.jforex.programming.order.process.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.process.MergeProcess;
import com.jforex.programming.test.common.CommonUtilForTest;

public class MergeProcessTest extends CommonUtilForTest {

    private final static String mergeOrderLabel = "MergeLabel";
    private final Collection<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Test
    public void assertActionsAreValidWhenNotDefined() {
        final MergeProcess mergeBuilder = MergeProcess
            .forParams(mergeOrderLabel, toMergeOrders)
            .build();

        assertNotNull(mergeBuilder.errorAction());
    }

    @Test
    public void assertValuesAreCorrect() {
        final Consumer<Throwable> errorAction = t -> {};
        final Consumer<IOrder> mergeRejectAction = o -> {};
        final Consumer<IOrder> mergeOKAction = o -> {};
        final Consumer<IOrder> mergeCloseOKAction = o -> {};

        final MergeProcess process = MergeProcess
            .forParams(mergeOrderLabel, toMergeOrders)
            .onError(errorAction)
            .onMergeReject(mergeRejectAction)
            .onMerge(mergeOKAction)
            .onMergeClose(mergeCloseOKAction)
            .build();

        assertThat(process.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(process.toMergeOrders(), equalTo(toMergeOrders));
        assertThat(process.errorAction(), equalTo(errorAction));
    }
}
