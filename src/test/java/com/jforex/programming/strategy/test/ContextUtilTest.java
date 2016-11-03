package com.jforex.programming.strategy.test;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.OfferSide;
import com.dukascopy.api.feed.IBarFeedListener;
import com.jforex.programming.strategy.ContextUtil;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class ContextUtilTest extends QuoteProviderForTest {

    private ContextUtil contextUtil;

    @Mock
    private IBarFeedListener barFeedListenerMock;

    @Before
    public void setUp() {
        contextUtil = new ContextUtil(contextMock);
    }

    @Test
    public void initBarsFeedCallsContext() {
        contextUtil.initBarsFeed(askBarEURUSDCustomPeriodParams, barFeedListenerMock);

        verify(contextMock).subscribeToBarsFeed(eq(instrumentEURUSD),
                                                eq(custom3MinutePeriod),
                                                eq(OfferSide.ASK),
                                                eq(barFeedListenerMock));
    }
}
