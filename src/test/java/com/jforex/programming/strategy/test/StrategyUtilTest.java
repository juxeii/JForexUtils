package com.jforex.programming.strategy.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.JFException;
import com.google.common.collect.Lists;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.strategy.StrategyUtil;
import com.jforex.programming.test.common.QuoteProviderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class StrategyUtilTest extends QuoteProviderForTest {

    private StrategyUtil strategyUtil;

    @Before
    public void setUp() {
        strategyUtil = new StrategyUtil(contextMock);
    }

    @Test
    public void returnedContextIsCorrectInstance() {
        assertThat(strategyUtil.context(), equalTo(contextMock));
    }

    @Test
    public void returnedEngineIsCorrectInstance() {
        assertThat(strategyUtil.engine(), equalTo(engineMock));
    }

    @Test
    public void returnedAccountIsCorrectInstance() {
        assertThat(strategyUtil.account(), equalTo(accountMock));
    }

    @Test
    public void returnedHistoryIsCorrectInstance() {
        assertThat(strategyUtil.history(), equalTo(historyMock));
    }

    @Test
    public void returnedHistoryUtilIsValid() {
        assertNotNull(strategyUtil.historyUtil());
    }

    @Test
    public void returnedCalculationUtilIsValid() {
        assertNotNull(strategyUtil.calculationUtil());
    }

    @Test
    public void returnedOrderUtilIsValid() {
        assertNotNull(strategyUtil.orderUtil());
    }

    @Test
    public void returnedPositionUtilIsValid() {
        assertNotNull(strategyUtil.positionUtil());
    }

    @Test
    public void returnedStrategyThreadRunnerIsValid() {
        assertNotNull(strategyUtil.strategyThreadRunner());
    }

    @Test
    public void onMessageRouting() {
        strategyUtil.onMessage(mock(IMessage.class));
    }

    @Test
    public void testIfStrategyThreadIsCorrect() {
        setStrategyThread();

        assertTrue(StrategyUtil.isStrategyThread());
    }

    @Test
    public void testIfNotStrategyThreadIsCorrect() {
        setNotStrategyThread();

        assertFalse(StrategyUtil.isStrategyThread());
    }

    @Test
    public void returnedThreadNameIsCorrect() {
        final String threadName = "TestThread";

        setThreadName(threadName);

        assertThat(StrategyUtil.threadName(), equalTo(threadName));
    }

    @Test
    public void testMarketIsClosed() {
        final long testTime = 1234L;

        when(dataServiceMock.isOfflineTime(testTime)).thenReturn(true);

        assertTrue(strategyUtil.isMarketClosed(testTime));
    }

    @Test
    public void testMarketIsOpened() {
        final long testTime = 1234L;

        when(dataServiceMock.isOfflineTime(testTime)).thenReturn(false);

        assertFalse(strategyUtil.isMarketClosed(testTime));
    }

    @Test
    public void coverIsMarketNowClosed() {
        strategyUtil.isMarketClosed();
    }

    @Test
    public void importOrdersDistributesOrdersCorrect() throws JFException {
        when(engineMock.getOrders()).thenReturn(Lists.newArrayList(buyOrderEURUSD,
                                                                   sellOrderEURUSD,
                                                                   sellOrderAUDUSD));

        strategyUtil
            .importOrders()
            .test()
            .assertComplete();

        final PositionOrders ordersForEURUSD = strategyUtil
            .orderUtil()
            .positionOrders(instrumentEURUSD);
        final PositionOrders ordersForAUDUSD = strategyUtil
            .orderUtil()
            .positionOrders(instrumentAUDUSD);

        assertThat(ordersForEURUSD.size(), equalTo(2));
        assertTrue(ordersForEURUSD.contains(buyOrderEURUSD));
        assertTrue(ordersForEURUSD.contains(sellOrderEURUSD));

        assertThat(ordersForAUDUSD.size(), equalTo(1));
        assertTrue(ordersForAUDUSD.contains(sellOrderAUDUSD));
    }

    public class AfterBarPushed {

        private BarQuoteProvider barQuoteProvider;
        private final TestObserver<BarQuote> subscriber = TestObserver.create();
        private Runnable pushBar;

        @Before
        public void setUp() {
            barQuoteProvider = strategyUtil.barQuoteProvider();
            barQuoteProvider
                .observable()
                .subscribe(subscriber);
            pushBar = () -> strategyUtil.onBar(instrumentEURUSD,
                                               barQuotePeriod,
                                               askBarEURUSD,
                                               bidBarEURUSD);
            pushBar.run();
        }

        @Test
        public void barIsObserved() {
            subscriber.assertNoErrors();
            subscriber.assertValueCount(2);

            assertThat(getOnNextEvent(subscriber, 0),
                       equalTo(askBarQuoteEURUSD));
            assertThat(getOnNextEvent(subscriber, 1),
                       equalTo(bidBarQuoteEURUSD));
        }

        @Test
        public void onStopUnsubscribesFromBars() {
            strategyUtil.onStop();

            pushBar.run();

            subscriber.assertValueCount(2);
        }

        @Test
        public void barIsNotPushedWhenMarketIsClosed() {
            when(dataServiceMock.isOfflineTime(anyLong())).thenReturn(true);

            pushBar.run();

            subscriber.assertValueCount(2);
        }
    }

    public class AfterTickPushed {

        private TickQuoteProvider tickQuoteProvider;
        private InstrumentUtil instrumentUtil;
        private final TestObserver<TickQuote> subscriber = TestObserver.create();

        @Before
        public void setUp() {
            tickQuoteProvider = strategyUtil.tickQuoteProvider();
            tickQuoteProvider
                .observable()
                .subscribe(subscriber);
            instrumentUtil = strategyUtil.instrumentUtil(instrumentEURUSD);

            strategyUtil.onTick(instrumentEURUSD, tickEURUSD);
        }

        @Test
        public void tickIsObserved() {
            subscriber.assertNoErrors();
            subscriber.assertValueCount(1);

            assertThat(getOnNextEvent(subscriber, 0),
                       equalTo(tickQuoteEURUSD));
        }

        @Test
        public void instrumentUtilHasTick() {
            assertThat(instrumentUtil.tickQuote(), equalTo(tickEURUSD));
        }

        @Test
        public void onStopUnsubscribesFromTicks() {
            strategyUtil.onStop();

            strategyUtil.onTick(instrumentEURUSD, tickEURUSD);

            subscriber.assertValueCount(1);
        }

        @Test
        public void tickIsNotPushedWhenMarketIsClosed() {
            when(dataServiceMock.isOfflineTime(anyLong())).thenReturn(true);

            strategyUtil.onTick(instrumentEURUSD, tickEURUSD);

            subscriber.assertValueCount(1);
        }
    }
}
