package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Sets;
import com.jforex.programming.quote.QuoteProviderException;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.test.common.CurrencyUtilForTest;
import com.jforex.programming.test.fakes.ITickForTest;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class TickQuoteHandlerTest extends CurrencyUtilForTest {

    private TickQuoteHandler tickQuoteHandler;

    private final Set<Instrument> subscribedInstruments = Sets.newHashSet(instrumentEURUSD, instrumentAUDUSD);
    private final Map<Instrument, ITick> latestTicks = new ConcurrentHashMap<>();
    private final ITick tickEURUSDOfHistory = new ITickForTest(1.23413, 1.23488);
    private final ITick firstTickEURUSD = new ITickForTest(1.23456, 1.23451);
    private final ITick firstTickAUDUSD = new ITickForTest(1.10345, 1.10348);

    @Before
    public void setUp() {
        initCommonTestFramework();
        setupMocks();

        tickQuoteHandler = new TickQuoteHandler(jforexUtilMock, historyUtilMock, subscribedInstruments);
    }

    private void setupMocks() {
        when(historyUtilMock.latestTick(instrumentEURUSD)).thenReturn(tickEURUSDOfHistory);
        when(historyUtilMock.latestTick(instrumentAUDUSD)).thenReturn(firstTickAUDUSD);

        latestTicks.put(instrumentEURUSD, tickEURUSDOfHistory);
        when(historyUtilMock.latestTicks(subscribedInstruments)).thenReturn(latestTicks);
    }

    private void verifyTickValues(final ITick tick) {
        assertThat(tickQuoteHandler.tick(instrumentEURUSD),
                   equalTo(tick));

        assertThat(tickQuoteHandler.ask(instrumentEURUSD),
                   equalTo(tick.getAsk()));

        assertThat(tickQuoteHandler.bid(instrumentEURUSD),
                   equalTo(tick.getBid()));

        assertThat(tickQuoteHandler.forOfferSide(instrumentEURUSD, OfferSide.ASK),
                   equalTo(tick.getAsk()));

        assertThat(tickQuoteHandler.forOfferSide(instrumentEURUSD, OfferSide.BID),
                   equalTo(tick.getBid()));
    }

    @Test(expected = QuoteProviderException.class)
    public void historyThrowsAtCreation() {
        when(historyUtilMock.latestTicks(subscribedInstruments)).thenThrow(QuoteProviderException.class);

        tickQuoteHandler = new TickQuoteHandler(jforexUtilMock, historyUtilMock, subscribedInstruments);
    }

    @Test
    public void historyReturnsTickBeforeFirstTickQuoteIsReceived() {
        when(historyUtilMock.latestTick(instrumentEURUSD)).thenReturn(tickEURUSDOfHistory);

        verifyTickValues(tickEURUSDOfHistory);
    }

    public class AfterFirstTickQuoteIsConsumed {

        @Before
        public void setUp() throws JFException {
            when(historyMock.getLastTick(instrumentEURUSD)).thenReturn(tickEURUSDOfHistory);

            tickQuoteHandler.onTick(instrumentEURUSD, firstTickEURUSD);
        }

        @Test
        public void tickReturnsReceivedTick() {
            assertThat(tickQuoteHandler.tick(instrumentEURUSD), equalTo(firstTickEURUSD));
        }

        @Test
        public void askReturnsReceivedAskOfTick() {
            assertThat(tickQuoteHandler.ask(instrumentEURUSD), equalTo(firstTickEURUSD.getAsk()));
        }

        @Test
        public void bidReturnsReceivedBidOfTick() {
            assertThat(tickQuoteHandler.bid(instrumentEURUSD), equalTo(firstTickEURUSD.getBid()));
        }

        @Test
        public void forAskOfferSideReturnsReceivedAskOfTick() {
            assertThat(tickQuoteHandler.forOfferSide(instrumentEURUSD, OfferSide.ASK),
                       equalTo(firstTickEURUSD.getAsk()));
        }

        @Test
        public void forBidOfferSideReturnsReceivedBidOfTick() {
            assertThat(tickQuoteHandler.forOfferSide(instrumentEURUSD, OfferSide.BID),
                       equalTo(firstTickEURUSD.getBid()));
        }

        public class SubscriptionToTick {

            private final TestSubscriber<TickQuote> quoteEURUSDSubscriber = new TestSubscriber<>();
            private final TestSubscriber<TickQuote> quoteAUDUSDSubscriber = new TestSubscriber<>();

            @Before
            public void setUp() {
                tickQuoteHandler
                        .quoteObservable(Sets.newHashSet(instrumentEURUSD))
                        .subscribe(quoteEURUSDSubscriber);

                tickQuoteHandler
                        .quoteObservable(Sets.newHashSet(instrumentAUDUSD))
                        .subscribe(quoteAUDUSDSubscriber);

                tickQuoteHandler.onTick(instrumentEURUSD, firstTickEURUSD);
            }

            @Test
            public void newTickIsEmitted() {
                quoteEURUSDSubscriber.assertNoErrors();
                quoteEURUSDSubscriber.assertValueCount(1);

                final TickQuote tickQuote = quoteEURUSDSubscriber.getOnNextEvents().get(0);
                assertThat(tickQuote.tick(), equalTo(firstTickEURUSD));
                assertThat(tickQuote.instrument(), equalTo(instrumentEURUSD));
            }

            @Test
            public void newTickIsNotEmittedForAUDUSDSubscriber() {
                quoteAUDUSDSubscriber.assertNoErrors();
                quoteAUDUSDSubscriber.assertValueCount(0);
            }
        }
    }
}
