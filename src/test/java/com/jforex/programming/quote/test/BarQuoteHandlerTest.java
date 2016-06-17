package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.Unit;
import com.google.common.collect.Sets;
import com.jforex.programming.builder.BarQuoteParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;

import rx.Observable;
import rx.observers.TestSubscriber;

public class BarQuoteHandlerTest extends QuoteProviderForTest {

    private BarQuoteHandler barQuoteHandler;

    @Mock
    private BarQuoteRepository barQuoteRepositoryMock;
    private final Period testPeriod = Period.ONE_MIN;
    private final Period custom3MinutePeriod = Period.createCustomPeriod(Unit.Minute, 3);
    private final BarQuote askQuoteEURUSD = new BarQuote(instrumentEURUSD, testPeriod, OfferSide.ASK, askBarEURUSD);
    private final BarQuote askQuoteAUDUSD = new BarQuote(instrumentAUDUSD, testPeriod, OfferSide.ASK, askBarAUDUSD);
    private final BarQuote askQuote5MinAUDUSD =
            new BarQuote(instrumentAUDUSD, Period.FIVE_MINS, OfferSide.ASK, askBarAUDUSD);
    private final Observable<BarQuote> quoteObservable =
            Observable.just(askQuoteEURUSD, askQuoteAUDUSD, askQuote5MinAUDUSD);
    private final TestSubscriber<BarQuote> quoteEURUSDAndAUDUSDSubscriber = new TestSubscriber<>();
    private final TestSubscriber<BarQuote> quoteGBPAUDSubscriber = new TestSubscriber<>();

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        barQuoteHandler = new BarQuoteHandler(jforexUtilMock,
                                              quoteObservable,
                                              barQuoteRepositoryMock);

        barQuoteHandler.observableForSubscription(BarQuoteParams
                .forInstruments(Sets.newHashSet(instrumentEURUSD, instrumentAUDUSD))
                .period(testPeriod)
                .offerSide(OfferSide.ASK))
                .subscribe(quoteEURUSDAndAUDUSDSubscriber);

        barQuoteHandler.observableForSubscription(BarQuoteParams
                .forInstruments(Sets.newHashSet(instrumentGBPAUD))
                .period(testPeriod)
                .offerSide(OfferSide.ASK))
                .subscribe(quoteGBPAUDSubscriber);

        barQuoteHandler.observableForSubscription(BarQuoteParams
                .forInstruments(Sets.newHashSet(instrumentEURUSD))
                .period(custom3MinutePeriod)
                .offerSide(OfferSide.ASK))
                .subscribe();
    }

    private void setUpMocks() {
        when(barQuoteRepositoryMock.get(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.ASK)))
                .thenReturn(askBarEURUSD);
        when(barQuoteRepositoryMock.get(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.BID)))
                .thenReturn(bidBarEURUSD);
        when(barQuoteRepositoryMock.get(eq(instrumentAUDUSD), eq(testPeriod), eq(OfferSide.ASK)))
                .thenReturn(askBarAUDUSD);
        when(barQuoteRepositoryMock.get(eq(instrumentAUDUSD), eq(testPeriod), eq(OfferSide.BID)))
                .thenReturn(bidBarAUDUSD);
    }

    @Test
    public void returnedAskBarIsCorrect() {
        assertThat(barQuoteHandler.askBar(instrumentEURUSD, testPeriod),
                   equalTo(askBarEURUSD));

        assertThat(barQuoteHandler.askBar(instrumentAUDUSD, testPeriod),
                   equalTo(askBarAUDUSD));
    }

    @Test
    public void returnedBidBarIsCorrect() {
        assertThat(barQuoteHandler.bidBar(instrumentEURUSD, testPeriod),
                   equalTo(bidBarEURUSD));

        assertThat(barQuoteHandler.bidBar(instrumentAUDUSD, testPeriod),
                   equalTo(bidBarAUDUSD));
    }

    @Test
    public void returnedAskBarForOfferSideIsCorrect() {
        assertThat(barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.ASK),
                   equalTo(askBarEURUSD));

        assertThat(barQuoteHandler.forOfferSide(instrumentAUDUSD, testPeriod, OfferSide.ASK),
                   equalTo(askBarAUDUSD));
    }

    @Test
    public void returnedBidBarForOfferSideIsCorrect() {
        assertThat(barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID),
                   equalTo(bidBarEURUSD));

        assertThat(barQuoteHandler.forOfferSide(instrumentAUDUSD, testPeriod, OfferSide.BID),
                   equalTo(bidBarAUDUSD));
    }

    @Test
    public void barsAreEmitted() {
        quoteEURUSDAndAUDUSDSubscriber.assertNoErrors();
        quoteEURUSDAndAUDUSDSubscriber.assertValueCount(2);

        final BarQuote barQuoteEURUSD = quoteEURUSDAndAUDUSDSubscriber.getOnNextEvents().get(0);
        assertThat(barQuoteEURUSD.instrument(), equalTo(instrumentEURUSD));
        assertThat(barQuoteEURUSD.period(), equalTo(testPeriod));
        assertThat(barQuoteEURUSD.offerSide(), equalTo(OfferSide.ASK));
        assertThat(barQuoteEURUSD.bar(), equalTo(askBarEURUSD));

        final BarQuote barQuoteAUDUSD = quoteEURUSDAndAUDUSDSubscriber.getOnNextEvents().get(1);
        assertThat(barQuoteAUDUSD.instrument(), equalTo(instrumentAUDUSD));
        assertThat(barQuoteAUDUSD.period(), equalTo(testPeriod));
        assertThat(barQuoteEURUSD.offerSide(), equalTo(OfferSide.ASK));
        assertThat(barQuoteAUDUSD.bar(), equalTo(askBarAUDUSD));
    }

    @Test
    public void barsAreNotEmittedForGBPAUDSubscriber() {
        quoteGBPAUDSubscriber.assertNoErrors();
        quoteGBPAUDSubscriber.assertValueCount(0);
    }

    @Test
    public void onCustomPeriodSubscriptionJForexUtilIsCalled() {
        verify(jforexUtilMock).subscribeToBarsFeed(eq(instrumentEURUSD),
                                                   eq(custom3MinutePeriod),
                                                   eq(OfferSide.ASK));
    }
}
