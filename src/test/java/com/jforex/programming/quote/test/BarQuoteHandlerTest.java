package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.builder.BarQuoteSubscription;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.QuoteProviderException;
import com.jforex.programming.test.common.CurrencyUtilForTest;

import com.dukascopy.api.IBar;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.Unit;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class BarQuoteHandlerTest extends CurrencyUtilForTest {

    private BarQuoteHandler barQuoteHandler;

    @Mock
    private IBar askBarEURUSDOfHistory;
    @Mock
    private IBar bidBarEURUSDOfHistory;
    @Mock
    private IBar firstAskBarEURUSD;
    @Mock
    private IBar firstBidBarEURUSD;
    @Mock
    private IBar secondAskBarEURUSD;
    @Mock
    private IBar secondBidBarEURUSD;
    @Mock
    private IBar firstAskBarAUDUSD;
    @Mock
    private IBar firstBidBarAUDUSD;
    private final Period testPeriod = Period.ONE_MIN;

    @Before
    public void setUp() {
        initCommonTestFramework();

        barQuoteHandler = new BarQuoteHandler(jforexUtilMock);
    }

    private void verifyBarValues(final IBar askBar,
                                 final IBar bidBar) {
        assertThat(barQuoteHandler.askBar(instrumentEURUSD, testPeriod),
                   equalTo(askBar));

        assertThat(barQuoteHandler.bidBar(instrumentEURUSD, testPeriod),
                   equalTo(bidBar));

        assertThat(barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.ASK),
                   equalTo(askBar));

        assertThat(barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID),
                   equalTo(bidBar));
    }

    private void setupHistoryBars() throws JFException {
        when(historyUtilMock.latestBar(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.ASK)))
                .thenReturn(askBarEURUSDOfHistory);
        when(historyUtilMock.latestBar(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.BID)))
                .thenReturn(bidBarEURUSDOfHistory);
    }

    @Test
    public void historyReturnsBarBeforeFirstBarQuoteIsReceived() throws JFException {
        setupHistoryBars();

        verifyBarValues(askBarEURUSDOfHistory, bidBarEURUSDOfHistory);
    }

    public class HistoryThrowsBeforeFirstBarQuoteIsReceived {

        @Before
        public void setUp() throws JFException {
            when(historyUtilMock.latestBar(eq(instrumentEURUSD), eq(testPeriod), any()))
                    .thenThrow(QuoteProviderException.class);
        }

        @Test(expected = QuoteProviderException.class)
        public void testAskBarThrows() {
            barQuoteHandler.askBar(instrumentEURUSD, testPeriod);
        }

        @Test(expected = QuoteProviderException.class)
        public void testBidBarThrows() {
            barQuoteHandler.bidBar(instrumentEURUSD, testPeriod);
        }

        @Test(expected = QuoteProviderException.class)
        public void testForOfferSideAskThrows() {
            barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.ASK);
        }

        @Test(expected = QuoteProviderException.class)
        public void testForOfferSideBidThrows() {
            barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID);
        }
    }

    public class HistoryReturnsNullBeforeFirstBarQuoteIsReceived {

        private void verifyHistoryRetry() throws JFException {
            verify(historyUtilMock).latestBar(eq(instrumentEURUSD), eq(testPeriod), any());
        }

        @Before
        public void setUp() throws JFException {
            when(historyUtilMock.latestBar(eq(instrumentEURUSD), eq(testPeriod), any()))
                    .thenReturn(null)
                    .thenReturn(askBarEURUSDOfHistory);
        }

        @Test
        public void testAskBarRetries() throws JFException {
            barQuoteHandler.askBar(instrumentEURUSD, testPeriod);

            verifyHistoryRetry();
        }

        @Test
        public void testBidBarRetries() throws JFException {
            barQuoteHandler.bidBar(instrumentEURUSD, testPeriod);

            verifyHistoryRetry();
        }

        @Test
        public void testForOfferSideAskRetries() throws JFException {
            barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.ASK);

            verifyHistoryRetry();
        }

        @Test
        public void testForOfferSideBidRetries() throws JFException {
            barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID);

            verifyHistoryRetry();
        }

        @Test(expected = QuoteProviderException.class)
        public void testAfterAllTriesExceptionIsThrown() throws JFException {
            when(historyUtilMock.latestBar(eq(instrumentEURUSD), eq(testPeriod), any()))
                    .thenThrow(QuoteProviderException.class);

            barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID);
        }
    }

    public class AskBarEURUSDIsConsumed {

        @Before
        public void setUp() throws JFException {
            setupHistoryBars();

            barQuoteHandler.onBar(instrumentEURUSD, testPeriod, OfferSide.ASK, firstAskBarEURUSD);
        }

        @Test
        public void askBarReturnsReceivedAskBar() {
            verifyZeroInteractions(historyUtilMock);

            assertThat(barQuoteHandler.askBar(instrumentEURUSD, testPeriod),
                       equalTo(firstAskBarEURUSD));
        }

        @Test
        public void bidBarReturnsHistoryBidBar() {
            assertThat(barQuoteHandler.bidBar(instrumentEURUSD, testPeriod),
                       equalTo(bidBarEURUSDOfHistory));
        }

        @Test
        public void forAskOfferSideReturnsReceivedAskBar() {
            verifyZeroInteractions(historyUtilMock);

            assertThat(barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.ASK),
                       equalTo(firstAskBarEURUSD));
        }

        @Test
        public void forBidOfferSideReturnsReturnsHistoryBidBar() {
            verifyZeroInteractions(historyUtilMock);

            assertThat(barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID),
                       equalTo(bidBarEURUSDOfHistory));
        }

        public class SubscriptionToAskBar {

            private final TestSubscriber<BarQuote> quoteEURUSDSubscriber = new TestSubscriber<>();
            private final TestSubscriber<BarQuote> customPeriodEURUSDSubscriber = new TestSubscriber<>();
            private final TestSubscriber<BarQuote> quoteAUDUSDSubscriber = new TestSubscriber<>();

            private final Period custom3MinutePeriod = Period.createCustomPeriod(Unit.Minute, 3);

            @Before
            public void setUp() {
                barQuoteHandler.quoteObservable(BarQuoteSubscription
                        .forInstruments(Sets.newHashSet(instrumentEURUSD))
                        .period(testPeriod)
                        .offerSide(OfferSide.ASK))
                        .subscribe(quoteEURUSDSubscriber);

                barQuoteHandler.quoteObservable(BarQuoteSubscription
                        .forInstruments(Sets.newHashSet(instrumentEURUSD))
                        .period(custom3MinutePeriod)
                        .offerSide(OfferSide.ASK))
                        .subscribe(customPeriodEURUSDSubscriber);

                barQuoteHandler.quoteObservable(BarQuoteSubscription
                        .forInstruments(Sets.newHashSet(instrumentAUDUSD))
                        .period(testPeriod)
                        .offerSide(OfferSide.ASK))
                        .subscribe(quoteAUDUSDSubscriber);

                barQuoteHandler.onBar(instrumentEURUSD, testPeriod, OfferSide.ASK, firstAskBarEURUSD);
                barQuoteHandler.onBar(instrumentEURUSD, custom3MinutePeriod, OfferSide.ASK, firstAskBarEURUSD);
            }

            @Test
            public void newBarIsEmitted() {
                quoteEURUSDSubscriber.assertNoErrors();
                quoteEURUSDSubscriber.assertValueCount(1);

                final BarQuote barQuote = quoteEURUSDSubscriber.getOnNextEvents().get(0);
                assertThat(barQuote.bar(), equalTo(firstAskBarEURUSD));
                assertThat(barQuote.instrument(), equalTo(instrumentEURUSD));
                assertThat(barQuote.period(), equalTo(testPeriod));
                assertThat(barQuote.offerSide(), equalTo(OfferSide.ASK));
            }

            @Test
            public void newCustomBarIsEmitted() {
                customPeriodEURUSDSubscriber.assertNoErrors();
                customPeriodEURUSDSubscriber.assertValueCount(1);

                final BarQuote barQuote = customPeriodEURUSDSubscriber.getOnNextEvents().get(0);
                assertThat(barQuote.bar(), equalTo(firstAskBarEURUSD));
                assertThat(barQuote.instrument(), equalTo(instrumentEURUSD));
                assertThat(barQuote.period(), equalTo(custom3MinutePeriod));
                assertThat(barQuote.offerSide(), equalTo(OfferSide.ASK));
            }

            @Test
            public void contextSubscribesToCustomBarFeed() {
                verify(contextMock).subscribeToBarsFeed(eq(instrumentEURUSD),
                                                        eq(custom3MinutePeriod),
                                                        eq(OfferSide.ASK),
                                                        any());
            }

            @Test
            public void newBarIsNotEmittedForAUDUSDSubscriber() {
                quoteAUDUSDSubscriber.assertNoErrors();
                quoteAUDUSDSubscriber.assertValueCount(0);
            }
        }

        public class BidkBarEURUSDIsConsumed {

            @Before
            public void setUp() {
                barQuoteHandler.onBar(instrumentEURUSD, testPeriod, OfferSide.BID, firstBidBarEURUSD);
            }

            @Test
            public void askBarReturnsReceivedAskBar() {
                verifyZeroInteractions(historyUtilMock);

                assertThat(barQuoteHandler.askBar(instrumentEURUSD, testPeriod),
                           equalTo(firstAskBarEURUSD));
            }

            @Test
            public void bidBarReturnsReceivedBidBar() {
                verifyZeroInteractions(historyUtilMock);

                assertThat(barQuoteHandler.bidBar(instrumentEURUSD, testPeriod),
                           equalTo(firstBidBarEURUSD));
            }
        }
    }
}
