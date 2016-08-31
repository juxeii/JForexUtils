package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.builder.ClosePositionProcess;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.test.common.QuoteProviderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class JForexUtilTest extends QuoteProviderForTest {

    private JForexUtil jForexUtil;

    @Before
    public void setUp() {
        jForexUtil = new JForexUtil(contextMock);
    }

    @Test
    public void returnedContextIsCorrectInstance() {
        assertThat(jForexUtil.context(), equalTo(contextMock));
    }

    @Test
    public void returnedEngineIsCorrectInstance() {
        assertThat(jForexUtil.engine(), equalTo(engineMock));
    }

    @Test
    public void returnedAccountIsCorrectInstance() {
        assertThat(jForexUtil.account(), equalTo(accountMock));
    }

    @Test
    public void returnedHistoryIsCorrectInstance() {
        assertThat(jForexUtil.history(), equalTo(historyMock));
    }

    @Test
    public void returnedHistoryUtilIsValid() {
        assertNotNull(jForexUtil.historyUtil());
    }

    @Test
    public void returnedCalculationUtilIsValid() {
        assertNotNull(jForexUtil.calculationUtil());
    }

    @Test
    public void onMessageRouting() {
        jForexUtil.onMessage(mock(IMessage.class));
    }

    @Test
    public void testIfStrategyThreadIsCorrect() {
        setStrategyThread();

        assertTrue(JForexUtil.isStrategyThread());
    }

    @Test
    public void testIfNotStrategyThreadIsCorrect() {
        setNotStrategyThread();

        assertFalse(JForexUtil.isStrategyThread());
    }

    @Test
    public void returnedThreadNameIsCorrect() {
        final String threadName = "TestThread";

        setThreadName(threadName);

        assertThat(JForexUtil.threadName(), equalTo(threadName));
    }

    @Test
    public void testMarketIsClosed() {
        final long testTime = 1234L;

        when(dataServiceMock.isOfflineTime(testTime)).thenReturn(true);

        assertTrue(jForexUtil.isMarketClosed(testTime));
    }

    @Test
    public void testMarketIsOpened() {
        final long testTime = 1234L;

        when(dataServiceMock.isOfflineTime(testTime)).thenReturn(false);

        assertFalse(jForexUtil.isMarketClosed(testTime));
    }

    @Test
    public void coverIsMarketNowClosed() {
        jForexUtil.isMarketClosed();
    }

    @Test
    public void subscriptionToBarsFeedCallContext() {
        jForexUtil.subscribeToBarsFeed(askBarEURUSDCustomPeriodParams);

        verify(contextMock).subscribeToBarsFeed(eq(instrumentEURUSD),
                                                eq(custom3MinutePeriod),
                                                eq(OfferSide.ASK),
                                                any());
    }

    public class AfterPositionRetreival {

        private OrderUtil orderUtil;
        private PositionOrders positionOrders;

        @Before
        public void setUp() {
            orderUtil = spy(jForexUtil.orderUtil());
            positionOrders = orderUtil.positionOrders(instrumentEURUSD);
        }

        @Test
        public void returnedPositionOrderSizeIsNull() {
            assertThat(positionOrders.size(), equalTo(0));
        }

        @Test
        public void coverCloseAllPositions() {
            final ClosePositionProcess builder = ClosePositionProcess
                    .forInstrument(instrumentEURUSD)
                    .build();
            jForexUtil.closeAllPositions(builder);
        }
    }

    public class AfterBarPushed {

        private BarQuoteProvider barQuoteProvider;
        private InstrumentUtil instrumentUtil;
        private final TestSubscriber<BarQuote> subscriber = new TestSubscriber<>();
        private Runnable pushBar;

        @Before
        public void setUp() {
            barQuoteProvider = jForexUtil.barQuoteProvider();
            barQuoteProvider.observable().subscribe(subscriber);
            instrumentUtil = jForexUtil.instrumentUtil(instrumentEURUSD);
            pushBar = () -> jForexUtil.onBar(instrumentEURUSD,
                                             barQuotePeriod,
                                             askBarEURUSD,
                                             bidBarEURUSD);
            pushBar.run();
        }

        @Test
        public void barIsObserved() {
            subscriber.assertNoErrors();
            subscriber.assertValueCount(2);

            assertEqualBarQuotes(subscriber.getOnNextEvents().get(0),
                                 askBarQuoteEURUSD);
            assertEqualBarQuotes(subscriber.getOnNextEvents().get(1),
                                 bidBarQuoteEURUSD);
        }

        @Test
        public void instrumentUtilHasAskBar() {
            final IBar bar = instrumentUtil.barQuote(askBarEURUSDParams);

            assertThat(bar, equalTo(askBarEURUSD));
        }

        @Test
        public void instrumentUtilHasBidBar() {
            assertThat(instrumentUtil.barQuote(bidBarEURUSDParams), equalTo(bidBarEURUSD));
        }

        @Test
        public void onStopUnsubscribesFromBars() {
            jForexUtil.onStop();

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
        private final TestSubscriber<TickQuote> subscriber = new TestSubscriber<>();

        @Before
        public void setUp() {
            tickQuoteProvider = jForexUtil.tickQuoteProvider();
            tickQuoteProvider.observable().subscribe(subscriber);
            instrumentUtil = jForexUtil.instrumentUtil(instrumentEURUSD);

            jForexUtil.onTick(instrumentEURUSD, tickEURUSD);
        }

        @Test
        public void tickIsObserved() {
            subscriber.assertNoErrors();
            subscriber.assertValueCount(1);

            assertEqualTickQuotes(subscriber.getOnNextEvents().get(0),
                                  tickQuoteEURUSD);
        }

        @Test
        public void instrumentUtilHasTick() {
            assertThat(instrumentUtil.tickQuote(), equalTo(tickEURUSD));
        }

        @Test
        public void onStopUnsubscribesFromTicks() {
            jForexUtil.onStop();

            jForexUtil.onTick(instrumentEURUSD, tickEURUSD);

            subscriber.assertValueCount(1);
        }

        @Test
        public void tickIsNotPushedWhenMarketIsClosed() {
            when(dataServiceMock.isOfflineTime(anyLong())).thenReturn(true);

            jForexUtil.onTick(instrumentEURUSD, tickEURUSD);

            subscriber.assertValueCount(1);
        }
    }
}
