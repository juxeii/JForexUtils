package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.BarQuoteParams;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;

import com.dukascopy.api.OfferSide;

import rx.Observable;
import rx.observers.TestSubscriber;

public class BarQuoteHandlerTest extends QuoteProviderForTest {

    private BarQuoteHandler barQuoteHandler;

    @Mock
    private BarQuoteRepository barQuoteRepositoryMock;
    private final TestSubscriber<BarQuote> filteredQuoteSubscriber = new TestSubscriber<>();
    private final TestSubscriber<BarQuote> unFilteredQuoteSubscriber = new TestSubscriber<>();
    private final Observable<BarQuote> quoteObservable =
            Observable.just(askBarQuoteEURUSD,
                            askBarQuoteAUDUSD,
                            askBarQuoteEURUSDCustomPeriod,
                            bidBarQuoteEURUSD);
    private final List<BarQuoteParams> quoteFilters = new ArrayList<>();

    @Before
    public void setUp() {
        initCommonTestFramework();

        barQuoteHandler = new BarQuoteHandler(jforexUtilMock,
                                              quoteObservable,
                                              barQuoteRepositoryMock);

        quoteFilters.add(askBarEURUSDParams);
        quoteFilters.add(askBarAUDUSDParams);

        barQuoteHandler.observableForFilters(quoteFilters).subscribe(filteredQuoteSubscriber);
        barQuoteHandler.observable().subscribe(unFilteredQuoteSubscriber);
    }

    private void assertCommonEmittedBars(final TestSubscriber<BarQuote> subscriber) {
        subscriber.assertNoErrors();

        assertThat(subscriber.getOnNextEvents().get(0),
                   equalTo(askBarQuoteEURUSD));
        assertThat(subscriber.getOnNextEvents().get(1),
                   equalTo(askBarQuoteAUDUSD));
    }

    @Test
    public void barQuoteParamsTest() {
        assertThat(askBarEURUSDParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(askBarEURUSDParams.period(), equalTo(barQuotePeriod));
        assertThat(askBarEURUSDParams.offerSide(), equalTo(OfferSide.ASK));
    }

    @Test
    public void returnedEURUSDBarIsCorrect() {
        when(barQuoteRepositoryMock.get(askBarEURUSDParams))
                .thenReturn(askBarQuoteEURUSD);

        assertThat(barQuoteHandler.bar(askBarEURUSDParams),
                   equalTo(askBarEURUSD));
    }

    @Test
    public void returnedAUDUSDBarIsCorrect() {
        when(barQuoteRepositoryMock.get(askBarAUDUSDParams))
                .thenReturn(askBarQuoteAUDUSD);

        assertThat(barQuoteHandler.bar(askBarAUDUSDParams),
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
                   equalTo(askBarQuoteEURUSDCustomPeriod));
        assertThat(unFilteredQuoteSubscriber.getOnNextEvents().get(3),
                   equalTo(bidBarQuoteEURUSD));
    }

    @Test
    public void onCustomPeriodSubscriptionJForexUtilIsCalled() {
        quoteFilters.add(askBarEURUSDParams);
        quoteFilters.add(askBarAUDUSDParams);
        quoteFilters.add(askBarEURUSDCustomPeriodParams);

        barQuoteHandler.observableForFilters(quoteFilters).subscribe(filteredQuoteSubscriber);

        verify(jforexUtilMock).subscribeToBarsFeed(askBarEURUSDCustomPeriodParams);
    }
}
