package com.jforex.programming.misc.test;

import org.junit.Test;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.JFSkeletonStrategy;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class JFSkeletonStrategyTest extends QuoteProviderForTest {

    private final JFSkeletonStrategy sekeletonStrategy = new JFSkeletonStrategy();

    @Test
    public void onlyCoverage() throws JFException {
        sekeletonStrategy.onStart(contextMock);
        sekeletonStrategy.onTick(instrumentEURUSD, tickEURUSD);
        sekeletonStrategy.onBar(instrumentEURUSD,
                                Period.DAILY,
                                askBarEURUSD,
                                bidBarEURUSD);
        sekeletonStrategy.onMessage(mockForIMessage(buyOrderEURUSD,
                                                    IMessage.Type.NOTIFICATION,
                                                    Sets.newHashSet()));
        sekeletonStrategy.onAccount(accountMock);
        sekeletonStrategy.onStop();
    }
}
