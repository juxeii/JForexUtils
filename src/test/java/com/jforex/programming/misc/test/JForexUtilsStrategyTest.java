package com.jforex.programming.misc.test;

import org.junit.Test;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.JForexUtilsStrategy;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class JForexUtilsStrategyTest extends QuoteProviderForTest {

    private class TestStrategy extends JForexUtilsStrategy {

        @Override
        protected void onJFStart(final IContext context) throws JFException {
        }

        @Override
        protected void onJFTick(final Instrument instrument,
                                final ITick tick) throws JFException {
        }

        @Override
        protected void onJFBar(final Instrument instrument,
                               final Period period,
                               final IBar askBar,
                               final IBar bidBar) throws JFException {
        }

        @Override
        protected void onJFMessage(final IMessage message) throws JFException {
        }

        @Override
        protected void onJFStop() throws JFException {
        }

        @Override
        protected void onJFAccount(final IAccount account) throws JFException {
        }
    }

    private final TestStrategy testStrategy = new TestStrategy();

    @Test
    public void onlyCoverage() throws JFException {
        testStrategy.onStart(contextMock);
        testStrategy.onStop();
        testStrategy.onTick(instrumentEURUSD, tickEURUSD);
        testStrategy.onBar(instrumentEURUSD,
                           barQuotePeriod,
                           askBarEURUSD,
                           bidBarEURUSD);
        testStrategy.onMessage(mockForIMessage(buyOrderEURUSD,
                                               IMessage.Type.CONNECTION_STATUS,
                                               Sets.newHashSet()));
        testStrategy.onAccount(accountMock);
    }
}
