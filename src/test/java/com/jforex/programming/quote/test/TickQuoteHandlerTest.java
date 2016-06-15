package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.google.common.collect.Sets;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.test.common.CurrencyUtilForTest;
import com.jforex.programming.test.fakes.ITickForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class TickQuoteHandlerTest extends CurrencyUtilForTest {

    private TickQuoteHandler tickQuoteHandler;

    private final Set<Instrument> subscribedInstruments = Sets.newHashSet(instrumentEURUSD, instrumentAUDUSD);
    private final ITick tickEURUSDOfHistory = new ITickForTest(1.23413, 1.23488);
    private final ITick firstTickEURUSD = new ITickForTest(1.23456, 1.23451);
    private final ITick firstTickAUDUSD = new ITickForTest(1.10345, 1.10348);

    @Before
    public void setUp() {
        initCommonTestFramework();
        setupMocks();

        tickQuoteHandler = new TickQuoteHandler(jforexUtilMock, subscribedInstruments);
    }

    private void setupMocks() {
        try {
            when(historyMock.getLastTick(instrumentEURUSD)).thenReturn(tickEURUSDOfHistory);
            when(historyMock.getLastTick(instrumentAUDUSD)).thenReturn(firstTickAUDUSD);
        } catch (final JFException e) {}
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

    public class HistoryThrowsAtInitialization {

        @Before
        public void setUp() throws JFException {
            when(historyMock.getLastTick(instrumentEURUSD)).thenThrow(jfException);
        }

        @Test(expected = Exception.class)
        public void testTickThrows() {
            tickQuoteHandler = new TickQuoteHandler(jforexUtilMock, subscribedInstruments);

            rxTestUtil.advanceTimeBy(5000L, TimeUnit.MILLISECONDS);
        }
    }

    public class HistoryReturnsTickBeforeFirstTickQuoteIsReceived {

        @Before
        public void setUp() throws JFException {
            when(historyMock.getLastTick(instrumentEURUSD)).thenReturn(tickEURUSDOfHistory);
        }

        @Test
        public void testTickReturnsHistoryTick() {
            verifyTickValues(tickEURUSDOfHistory);
        }
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
