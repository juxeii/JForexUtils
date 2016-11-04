package com.jforex.programming.order.spec.test;

import java.util.Set;

import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.task.MergeTask;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class ComplexMergeSpecTest extends QuoteProviderForTest {

    @Mock
    private MergeTask mergeTaskMock;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final String mergeOrderLabel = "mergeOrderLabel";

//    @Test
//    public void whenNotStartedNoSubscription() {
//        ComplexMergeSpec.forMerge(mergeOrderLabel,
//                                  toMergeOrders,
//                                  mergeTaskMock)
//            .doOnStart(() -> logger.info("Subscribing for complex merge"))
//            .start();
//    }
}
