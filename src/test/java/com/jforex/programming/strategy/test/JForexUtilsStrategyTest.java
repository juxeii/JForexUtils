package com.jforex.programming.strategy.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.google.common.collect.Sets;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.strategy.JForexUtilsStrategy;
import com.jforex.programming.strategy.StrategyUtil;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class JForexUtilsStrategyTest extends QuoteProviderForTest {

    private class TestStrategy extends JForexUtilsStrategy {

        public void setStrategyUtil(final StrategyUtil strategyUtil) {
            this.strategyUtil = strategyUtil;
        }

        public StrategyUtil getStrategyUtil() {
            return strategyUtil;
        }

        public OrderUtil getOrderUtil() {
            return orderUtil;
        }

        public PositionUtil getPositionUtil() {
            return positionUtil;
        }

        public CalculationUtil getCalculationUtil() {
            return calculationUtil;
        }

        @Override
        public void onJFStart(final IContext context) throws JFException {
        }

        @Override
        public void onJFTick(final Instrument instrument,
                             final ITick tick) throws JFException {
        }

        @Override
        public void onJFBar(final Instrument instrument,
                            final Period period,
                            final IBar askBar,
                            final IBar bidBar) throws JFException {
        }

        @Override
        public void onJFMessage(final IMessage message) throws JFException {
        }

        @Override
        public void onJFStop() throws JFException {
        }

        @Override
        public void onJFAccount(final IAccount account) throws JFException {
        }
    }

    private TestStrategy testStrategy;
    @Mock
    private StrategyUtil strategyUtilMock;

    @Before
    public void setUp() throws JFException {
        testStrategy = new TestStrategy();

        testStrategy.onStart(contextMock);
        baseUtilsAreInitialized();

        testStrategy.setStrategyUtil(strategyUtilMock);
        testStrategy.onAccount(accountMock);
    }

    @Test
    public void baseUtilsAreInitialized() throws JFException {
        assertNotNull(testStrategy.getStrategyUtil());
        assertNotNull(testStrategy.getOrderUtil());
        assertNotNull(testStrategy.getPositionUtil());
        assertNotNull(testStrategy.getCalculationUtil());
    }

    @Test
    public void onStopCallsStrategyUtil() throws JFException {
        testStrategy.onStop();
        verify(strategyUtilMock).onStop();
    }

    @Test
    public void onTickCallsStrategyUtil() throws JFException {
        testStrategy.onTick(instrumentEURUSD, tickEURUSD);
        verify(strategyUtilMock).onTick(instrumentEURUSD, tickEURUSD);
    }

    @Test
    public void onBarCallsStrategyUtil() throws JFException {
        testStrategy.onBar(instrumentEURUSD,
                           barQuotePeriod,
                           askBarEURUSD,
                           bidBarEURUSD);
        verify(strategyUtilMock).onBar(instrumentEURUSD,
                                       barQuotePeriod,
                                       askBarEURUSD,
                                       bidBarEURUSD);
    }

    @Test
    public void onMessageCallsStrategyUtil() throws JFException {
        final IMessage message = mockForIMessage(buyOrderEURUSD,
                                                 IMessage.Type.CONNECTION_STATUS,
                                                 Sets.newHashSet());
        testStrategy.onMessage(message);
        verify(strategyUtilMock).onMessage(message);
    }
}
