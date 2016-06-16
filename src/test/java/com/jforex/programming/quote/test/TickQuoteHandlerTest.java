package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.ITick;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.google.common.collect.Sets;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;
import com.jforex.programming.test.fakes.ITickForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class TickQuoteHandlerTest extends QuoteProviderForTest {

    private TickQuoteHandler tickQuoteHandler;

    @Mock
    private TickQuoteRepository tickQuoteRepositoryMock;
    private final ITick tickEURUSDOfHistory = new ITickForTest(1.23413, 1.23488);
    private final ITick firstTickEURUSD = tickEURUSD;
    private final ITick firstTickAUDUSD = tickAUDUSD;
    private final Subject<TickQuote, TickQuote> tickQuoteSubject = PublishSubject.create();

    @Before
    public void setUp() {
        initCommonTestFramework();
        setupMocks();

        tickQuoteHandler = new TickQuoteHandler(tickQuoteSubject, tickQuoteRepositoryMock);
    }

    private void setupMocks() {
        when(tickQuoteRepositoryMock.get(instrumentEURUSD)).thenReturn(tickEURUSDOfHistory);
        when(tickQuoteRepositoryMock.get(instrumentAUDUSD)).thenReturn(firstTickAUDUSD);
    }

    public class AfterFirstTickQuoteIsConsumed {

        @Before
        public void setUp() throws JFException {
            when(tickQuoteRepositoryMock.get(instrumentEURUSD)).thenReturn(firstTickEURUSD);
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
                        .observableForInstruments(Sets.newHashSet(instrumentEURUSD))
                        .subscribe(quoteEURUSDSubscriber);

                tickQuoteHandler
                        .observableForInstruments(Sets.newHashSet(instrumentAUDUSD))
                        .subscribe(quoteAUDUSDSubscriber);

                tickQuoteSubject.onNext(new TickQuote(instrumentEURUSD, firstTickEURUSD));
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
