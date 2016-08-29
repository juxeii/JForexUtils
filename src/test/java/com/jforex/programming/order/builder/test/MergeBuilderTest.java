package com.jforex.programming.order.builder.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.builder.MergeBuilder;
import com.jforex.programming.test.common.CommonUtilForTest;

public class MergeBuilderTest extends CommonUtilForTest {

    private final static String mergeOrderLabel = "MergeLabel";
    private final Collection<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Test
    public void assertActionsAreValidWhenNotDefined() {
        final MergeBuilder mergeBuilder = MergeBuilder
                .forParams(mergeOrderLabel, toMergeOrders)
                .build();

        assertNotNull(mergeBuilder.errorAction());
        assertNotNull(mergeBuilder.mergeRejectAction());
        assertNotNull(mergeBuilder.mergeOKAction());
        assertNotNull(mergeBuilder.mergeCloseOKAction());
    }

    @Test
    public void assertValuesAreCorrect() {
        final Consumer<Throwable> errorAction = t -> {};
        final Consumer<IOrder> mergeRejectAction = o -> {};
        final Consumer<IOrder> mergeOKAction = o -> {};
        final Consumer<IOrder> mergeCloseOKAction = o -> {};

        final MergeBuilder mergeBuilder = MergeBuilder
                .forParams(mergeOrderLabel, toMergeOrders)
                .onError(errorAction)
                .onMergeReject(mergeRejectAction)
                .onMergeOK(mergeOKAction)
                .onMergeCloseOK(mergeCloseOKAction)
                .build();

        assertThat(mergeBuilder.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergeBuilder.toMergeOrders(), equalTo(toMergeOrders));
        assertThat(mergeBuilder.errorAction(), equalTo(errorAction));
        assertThat(mergeBuilder.mergeRejectAction(), equalTo(mergeRejectAction));
        assertThat(mergeBuilder.mergeOKAction(), equalTo(mergeOKAction));
        assertThat(mergeBuilder.mergeCloseOKAction(), equalTo(mergeCloseOKAction));
    }
}
