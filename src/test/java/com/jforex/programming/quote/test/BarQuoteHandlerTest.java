package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.Unit;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.BarQuoteParams;
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
    private final TestSubscriber<BarQuote> filteredQuoteSubscriber = new TestSubscriber<>();
    private final TestSubscriber<BarQuote> unFilteredQuoteSubscriber = new TestSubscriber<>();
    private final BarQuoteParams quoteEURUSDParams = BarQuoteParams
            .forInstrument(instrumentEURUSD)
            .period(testPeriod)
            .offerSide(OfferSide.ASK);
    private final BarQuoteParams quoteEURUSDParamsBID = BarQuoteParams
            .forInstrument(instrumentEURUSD)
            .period(testPeriod)
            .offerSide(OfferSide.BID);
    private final BarQuoteParams quoteAUDUSDParams = BarQuoteParams
            .forInstrument(instrumentAUDUSD)
            .period(testPeriod)
            .offerSide(OfferSide.ASK);
    private final BarQuoteParams quoteEURUSDCustomPeriodParams = BarQuoteParams
            .forInstrument(instrumentEURUSD)
            .period(custom3MinutePeriod)
            .offerSide(OfferSide.ASK);
    private final BarQuote askQuoteEURUSD = new BarQuote(quoteEURUSDParams, askBarEURUSD);
    private final BarQuote bidQuoteEURUSD = new BarQuote(quoteEURUSDParamsBID, bidBarEURUSD);
    private final BarQuote askQuoteEURUSDCustomPeriod =
            new BarQuote(quoteEURUSDCustomPeriodParams, askBarEURUSD);
    private final BarQuote askQuoteAUDUSD = new BarQuote(quoteAUDUSDParams, askBarAUDUSD);
    private final Observable<BarQuote> quoteObservable =
            Observable.just(askQuoteEURUSD,
                            askQuoteAUDUSD,
                            askQuoteEURUSDCustomPeriod,
                            bidQuoteEURUSD);
    private final List<BarQuoteParams> quoteFilters = new ArrayList<>();

    @Before
    public void setUp() {
        initCommonTestFramework();

        barQuoteHandler = new BarQuoteHandler(jforexUtilMock,
                                              quoteObservable,
                                              barQuoteRepositoryMock);

        quoteFilters.add(quoteEURUSDParams);
        quoteFilters.add(quoteAUDUSDParams);

        barQuoteHandler.observableForFilters(quoteFilters).subscribe(filteredQuoteSubscriber);
        barQuoteHandler.observable().subscribe(unFilteredQuoteSubscriber);
    }

    private void assertCommonEmittedBars(final TestSubscriber<BarQuote> subscriber) {
        subscriber.assertNoErrors();

        assertThat(subscriber.getOnNextEvents().get(0),
                   equalTo(askQuoteEURUSD));
        assertThat(subscriber.getOnNextEvents().get(1),
                   equalTo(askQuoteAUDUSD));
    }

    @Test
    public void returnedEURUSDBarIsCorrect() {
        when(barQuoteRepositoryMock.get(quoteEURUSDParams))
                .thenReturn(askQuoteEURUSD);

        assertThat(barQuoteHandler.quote(quoteEURUSDParams),
                   equalTo(askBarEURUSD));
    }

    @Test
    public void returnedAUDUSDBarIsCorrect() {
        when(barQuoteRepositoryMock.get(quoteAUDUSDParams))
                .thenReturn(askQuoteAUDUSD);

        assertThat(barQuoteHandler.quote(quoteAUDUSDParams),
                   equalTo(askBarAUDUSD));
    }

    @Test
    public void filteredBarsAreEmitted() {
        filteredQuoteSubscriber.assertValueCount(2);

        assertCommonEmittedBars(filteredQuoteSubscriber);
    }

    @Test
    public void unFilteredBarsAreEmitted() {
        unFilteredQuoteSubscriber.assertValueCount(4);

        assertCommonEmittedBars(unFilteredQuoteSubscriber);

        assertThat(unFilteredQuoteSubscriber.getOnNextEvents().get(2),
                   equalTo(askQuoteEURUSDCustomPeriod));
        assertThat(unFilteredQuoteSubscriber.getOnNextEvents().get(3),
                   equalTo(bidQuoteEURUSD));
    }

    @Test
    public void onCustomPeriodSubscriptionJForexUtilIsCalled() {
        quoteFilters.add(quoteEURUSDParams);
        quoteFilters.add(quoteAUDUSDParams);
        quoteFilters.add(quoteEURUSDCustomPeriodParams);

        barQuoteHandler.observableForFilters(quoteFilters).subscribe(filteredQuoteSubscriber);

        verify(jforexUtilMock).subscribeToBarsFeed(quoteEURUSDCustomPeriodParams);
    }
}
