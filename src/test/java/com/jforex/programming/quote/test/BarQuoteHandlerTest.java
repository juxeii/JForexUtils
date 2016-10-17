package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.OfferSide;
import com.jforex.programming.init.QuoteUtil;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

public class BarQuoteHandlerTest extends QuoteProviderForTest {

    private BarQuoteProvider barQuoteHandler;

    @Mock
    private QuoteUtil quoteUtilMock;
    @Mock
    private BarQuoteRepository barQuoteRepositoryMock;
    private final TestObserver<BarQuote> filteredQuoteSubscriber = TestObserver.create();
    private final TestObserver<BarQuote> unFilteredQuoteSubscriber = TestObserver.create();
    private final Observable<BarQuote> quoteObservable =
            Observable.just(askBarQuoteEURUSD,
                            askBarQuoteAUDUSD,
                            askBarQuoteEURUSDCustomPeriod,
                            bidBarQuoteEURUSD);
    private final List<BarParams> quoteFilters = new ArrayList<>();

    @Before
    public void setUp() {
        barQuoteHandler = new BarQuoteProvider(quoteUtilMock,
                                               quoteObservable,
                                               barQuoteRepositoryMock);

        quoteFilters.add(askBarEURUSDParams);
        quoteFilters.add(askBarAUDUSDParams);

        barQuoteHandler
            .observableForParamsList(quoteFilters)
            .subscribe(filteredQuoteSubscriber);

        barQuoteHandler
            .observable()
            .subscribe(unFilteredQuoteSubscriber);
    }

    private void assertCommonEmittedBars(final TestObserver<BarQuote> subscriber) {
        subscriber.assertNoErrors();

        assertThat(getOnNextEvent(subscriber, 0),
                   equalTo(askBarQuoteEURUSD));
        assertThat(getOnNextEvent(subscriber, 1),
                   equalTo(askBarQuoteAUDUSD));
    }

    @Test
    public void barParamsTest() {
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

        assertThat(getOnNextEvent(unFilteredQuoteSubscriber, 2),
                   equalTo(askBarQuoteEURUSDCustomPeriod));
        assertThat(getOnNextEvent(unFilteredQuoteSubscriber, 3),
                   equalTo(bidBarQuoteEURUSD));
    }

    @Test
    public void onCustomPeriodSubscriptionQuoteUtilIsCalled() {
        quoteFilters.add(askBarEURUSDParams);
        quoteFilters.add(askBarAUDUSDParams);
        quoteFilters.add(askBarEURUSDCustomPeriodParams);

        barQuoteHandler
            .observableForParamsList(quoteFilters)
            .subscribe(filteredQuoteSubscriber);

        verify(quoteUtilMock).subscribeToBarsFeed(askBarEURUSDCustomPeriodParams);
    }
}
