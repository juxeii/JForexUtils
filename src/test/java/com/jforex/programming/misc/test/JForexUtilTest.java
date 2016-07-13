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
import com.dukascopy.api.Period;
import com.dukascopy.api.Unit;
import com.google.common.collect.Sets;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteParams;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.test.common.QuoteProviderForTest;
import com.jforex.programming.test.fakes.IBarForTest;
import com.jforex.programming.test.fakes.IMessageForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class JForexUtilTest extends QuoteProviderForTest {

    private JForexUtil jForexUtil;

    @Before
    public void setUp() {
        initCommonTestFramework();

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
    public void returnedRiskPercentMMIsValid() {
        assertNotNull(jForexUtil.riskPercentMM());
    }

    @Test
    public void returnedCalculationUtilIsValid() {
        assertNotNull(jForexUtil.calculationUtil());
    }

    @Test
    public void onMessageRouting() {
        final IMessage message = new IMessageForTest(IOrderForTest.buyOrderEURUSD(),
                                                     IMessage.Type.ORDER_CHANGED_REJECTED,
                                                     Sets.newHashSet());

        jForexUtil.onMessage(message);
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
        final Period custom3MinutePeriod = Period.createCustomPeriod(Unit.Minute, 3);
        final BarQuoteParams barQuoteParams = BarQuoteParams
                .forInstrument(instrumentEURUSD)
                .period(custom3MinutePeriod)
                .offerSide(OfferSide.ASK);

        jForexUtil.subscribeToBarsFeed(barQuoteParams);

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
            jForexUtil.closeAllPositions();
        }
    }

    public class AfterBarPushed {

        private BarQuoteProvider barQuoteProvider;
        private InstrumentUtil instrumentUtil;
        private final TestSubscriber<BarQuote> subscriber = new TestSubscriber<>();
        private final IBarForTest askBar = new IBarForTest();
        private final IBarForTest bidBar = new IBarForTest();
        private final BarQuoteParams askBarQuoteParams = BarQuoteParams
                .forInstrument(instrumentEURUSD)
                .period(Period.ONE_MIN)
                .offerSide(OfferSide.ASK);
        private final BarQuoteParams bidBarQuoteParams = BarQuoteParams
                .forInstrument(instrumentEURUSD)
                .period(Period.ONE_MIN)
                .offerSide(OfferSide.BID);

        @Before
        public void setUp() {
            barQuoteProvider = jForexUtil.barQuoteProvider();
            barQuoteProvider.observable().subscribe(subscriber);
            instrumentUtil = jForexUtil.instrumentUtil(instrumentEURUSD);

            jForexUtil.onBar(instrumentEURUSD,
                             Period.ONE_MIN,
                             askBar,
                             bidBar);
        }

        @Test
        public void barIsObserved() {
            subscriber.assertNoErrors();
            subscriber.assertValueCount(2);

            final BarQuote askBarQuote = subscriber.getOnNextEvents().get(0);
            assertThat(askBarQuote.instrument(), equalTo(instrumentEURUSD));
            assertThat(askBarQuote.period(), equalTo(Period.ONE_MIN));
            assertThat(askBarQuote.offerSide(), equalTo(OfferSide.ASK));
            assertThat(askBarQuote.bar(), equalTo(askBar));

            final BarQuote bidBarQuote = subscriber.getOnNextEvents().get(1);
            assertThat(bidBarQuote.instrument(), equalTo(instrumentEURUSD));
            assertThat(bidBarQuote.period(), equalTo(Period.ONE_MIN));
            assertThat(bidBarQuote.offerSide(), equalTo(OfferSide.BID));
            assertThat(bidBarQuote.bar(), equalTo(bidBar));
        }

        @Test
        public void instrumentUtilHasAskBar() {
            final IBar bar = instrumentUtil.bar(askBarQuoteParams);

            assertThat(bar, equalTo(askBar));
        }

        @Test
        public void instrumentUtilHasBidBar() {
            assertThat(instrumentUtil.bar(bidBarQuoteParams), equalTo(bidBar));
        }

        @Test
        public void onStopUnsubscribesFromBars() {
            jForexUtil.onStop();

            jForexUtil.onBar(instrumentEURUSD,
                             Period.ONE_MIN,
                             askBar,
                             bidBar);

            subscriber.assertValueCount(2);
        }

        @Test
        public void barIsNotPushedWhenMarketIsClosed() {
            when(dataServiceMock.isOfflineTime(anyLong())).thenReturn(true);

            jForexUtil.onBar(instrumentEURUSD,
                             Period.ONE_MIN,
                             askBar,
                             bidBar);

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

            final TickQuote tickQuote = subscriber.getOnNextEvents().get(0);
            assertThat(tickQuote.instrument(), equalTo(instrumentEURUSD));
            assertThat(tickQuote.tick(), equalTo(tickEURUSD));
        }

        @Test
        public void instrumentUtilHasTick() {
            assertThat(instrumentUtil.tick(), equalTo(tickEURUSD));
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
