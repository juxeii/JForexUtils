package com.jforex.programming.quote.test;

import static info.solidsoft.mockito.java8.LambdaMatcher.argLambda;
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
import com.jforex.programming.builder.BarQuoteFilter;
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
    private final TestSubscriber<BarQuote> quoteSubscriber = new TestSubscriber<>();
    private final BarQuoteFilter filterEURUSD = BarQuoteFilter
            .forInstrument(instrumentEURUSD)
            .period(testPeriod)
            .offerSide(OfferSide.ASK);
    private final BarQuoteFilter filterAUDUSD = BarQuoteFilter
            .forInstrument(instrumentAUDUSD)
            .period(testPeriod)
            .offerSide(OfferSide.ASK);
    private final BarQuoteFilter filterGBPAUD = BarQuoteFilter
            .forInstrument(instrumentGBPAUD)
            .period(testPeriod)
            .offerSide(OfferSide.ASK);
    private final BarQuoteFilter filterEURUSDCustomPeriod = BarQuoteFilter
            .forInstrument(instrumentEURUSD)
            .period(custom3MinutePeriod)
            .offerSide(OfferSide.ASK);
    private final List<BarQuoteFilter> quoteFilters = new ArrayList<>();

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        barQuoteHandler = new BarQuoteHandler(jforexUtilMock,
                                              quoteObservable,
                                              barQuoteRepositoryMock);

        quoteFilters.add(filterEURUSD);
        quoteFilters.add(filterAUDUSD);
        quoteFilters.add(filterGBPAUD);
        quoteFilters.add(filterEURUSDCustomPeriod);

        barQuoteHandler.observableForFilters(quoteFilters).subscribe(quoteSubscriber);
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
    public void returnedBarIsCorrect() {
        assertThat(barQuoteHandler.quote(filterEURUSD),
                   equalTo(askBarEURUSD));

        assertThat(barQuoteHandler.quote(filterAUDUSD),
                   equalTo(askBarAUDUSD));
    }

    @Test
    public void barsAreEmitted() {
        quoteSubscriber.assertNoErrors();
        quoteSubscriber.assertValueCount(2);

        final BarQuote barQuoteEURUSD = quoteSubscriber.getOnNextEvents().get(0);
        assertThat(barQuoteEURUSD.instrument(), equalTo(instrumentEURUSD));
        assertThat(barQuoteEURUSD.period(), equalTo(testPeriod));
        assertThat(barQuoteEURUSD.offerSide(), equalTo(OfferSide.ASK));
        assertThat(barQuoteEURUSD.bar(), equalTo(askBarEURUSD));

        final BarQuote barQuoteAUDUSD = quoteSubscriber.getOnNextEvents().get(1);
        assertThat(barQuoteAUDUSD.instrument(), equalTo(instrumentAUDUSD));
        assertThat(barQuoteAUDUSD.period(), equalTo(testPeriod));
        assertThat(barQuoteEURUSD.offerSide(), equalTo(OfferSide.ASK));
        assertThat(barQuoteAUDUSD.bar(), equalTo(askBarAUDUSD));
    }

    @Test
    public void onCustomPeriodSubscriptionJForexUtilIsCalled() {
        verify(jforexUtilMock).subscribeToBarsFeed(argLambda(td -> td.instrument() == instrumentEURUSD
                && td.period() == custom3MinutePeriod
                && td.offerSide() == OfferSide.ASK));
    }
}
