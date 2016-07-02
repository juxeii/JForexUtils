package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;

import com.dukascopy.api.OfferSide;

import rx.Observable;
import rx.observers.TestSubscriber;

public class TickQuoteHandlerTest extends QuoteProviderForTest {

    private TickQuoteHandler tickQuoteHandler;

    @Mock
    private TickQuoteRepository tickQuoteRepositoryMock;
    private final Observable<TickQuote> quoteObservable = Observable.just(tickQuoteEURUSD, tickQuoteAUDUSD);
    private final TestSubscriber<TickQuote> quoteEURUSDAndAUDUSDSubscriber = new TestSubscriber<>();
    private final TestSubscriber<TickQuote> quoteGBPAUDSubscriber = new TestSubscriber<>();

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        tickQuoteHandler = new TickQuoteHandler(quoteObservable, tickQuoteRepositoryMock);

        tickQuoteHandler
                .observableForInstruments(Sets.newHashSet(instrumentEURUSD, instrumentAUDUSD))
                .subscribe(quoteEURUSDAndAUDUSDSubscriber);
        tickQuoteHandler
                .observableForInstruments(Sets.newHashSet(instrumentGBPAUD))
                .subscribe(quoteGBPAUDSubscriber);
    }

    private void setUpMocks() {
        when(tickQuoteRepositoryMock.get(instrumentEURUSD)).thenReturn(new TickQuote(instrumentEURUSD, tickEURUSD));
        when(tickQuoteRepositoryMock.get(instrumentAUDUSD)).thenReturn(new TickQuote(instrumentAUDUSD, tickAUDUSD));
    }

    @Test
    public void returnedTickIsCorrect() {
        assertThat(tickQuoteHandler.tick(instrumentEURUSD),
                   equalTo(tickEURUSD));
    }

    @Test
    public void returnedAskIsCorrect() {
        assertThat(tickQuoteHandler.ask(instrumentEURUSD),
                   equalTo(tickEURUSD.getAsk()));
    }

    @Test
    public void returnedBidIsCorrect() {
        assertThat(tickQuoteHandler.bid(instrumentEURUSD),
                   equalTo(tickEURUSD.getBid()));
    }

    @Test
    public void returnedAskForOfferSideIsCorrect() {
        assertThat(tickQuoteHandler.forOfferSide(instrumentEURUSD, OfferSide.ASK),
                   equalTo(tickEURUSD.getAsk()));
    }

    @Test
    public void returnedBidForOfferSideIsCorrect() {
        assertThat(tickQuoteHandler.forOfferSide(instrumentEURUSD, OfferSide.BID),
                   equalTo(tickEURUSD.getBid()));
    }

    @Test
    public void ticksAreEmitted() {
        quoteEURUSDAndAUDUSDSubscriber.assertNoErrors();
        quoteEURUSDAndAUDUSDSubscriber.assertValueCount(2);

        final TickQuote tickQuoteEURUSD = quoteEURUSDAndAUDUSDSubscriber.getOnNextEvents().get(0);
        assertThat(tickQuoteEURUSD.tick(), equalTo(tickEURUSD));
        assertThat(tickQuoteEURUSD.instrument(), equalTo(instrumentEURUSD));

        final TickQuote tickQuoteAUDUSD = quoteEURUSDAndAUDUSDSubscriber.getOnNextEvents().get(1);
        assertThat(tickQuoteAUDUSD.tick(), equalTo(tickAUDUSD));
        assertThat(tickQuoteAUDUSD.instrument(), equalTo(instrumentAUDUSD));
    }

    @Test
    public void ticksAreNotEmittedForGBPAUDSubscriber() {
        quoteGBPAUDSubscriber.assertNoErrors();
        quoteGBPAUDSubscriber.assertValueCount(0);
    }
}
