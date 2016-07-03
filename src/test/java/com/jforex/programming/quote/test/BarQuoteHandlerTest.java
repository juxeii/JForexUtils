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
import com.dukascopy.api.Period;
import com.dukascopy.api.Unit;

import rx.Observable;
import rx.observers.TestSubscriber;

public class BarQuoteHandlerTest extends QuoteProviderForTest {

    private BarQuoteHandler barQuoteHandler;

    @Mock
    private BarQuoteRepository barQuoteRepositoryMock;
    private final Period testPeriod = Period.ONE_MIN;
    private final Period custom3MinutePeriod = Period.createCustomPeriod(Unit.Minute, 3);
    private final TestSubscriber<BarQuote> quoteSubscriber = new TestSubscriber<>();
    private final BarQuoteParams quoteEURUSDParams = BarQuoteParams
            .forInstrument(instrumentEURUSD)
            .period(testPeriod)
            .offerSide(OfferSide.ASK);
    private final BarQuoteParams quoteAUDUSDParams = BarQuoteParams
            .forInstrument(instrumentAUDUSD)
            .period(testPeriod)
            .offerSide(OfferSide.ASK);
    private final BarQuoteParams quoteEURUSDCustomPeriodParams = BarQuoteParams
            .forInstrument(instrumentEURUSD)
            .period(custom3MinutePeriod)
            .offerSide(OfferSide.ASK);
    private final BarQuote askQuoteEURUSD = new BarQuote(quoteEURUSDParams, askBarEURUSD);
    private final BarQuote askQuoteAUDUSD = new BarQuote(quoteAUDUSDParams, askBarAUDUSD);
    private final Observable<BarQuote> quoteObservable =
            Observable.just(askQuoteEURUSD, askQuoteAUDUSD);
    private final List<BarQuoteParams> quoteFilters = new ArrayList<>();

    @Before
    public void setUp() {
        initCommonTestFramework();

        barQuoteHandler = new BarQuoteHandler(jforexUtilMock,
                                              quoteObservable,
                                              barQuoteRepositoryMock);

        quoteFilters.add(quoteEURUSDParams);
        quoteFilters.add(quoteAUDUSDParams);
        quoteFilters.add(quoteEURUSDCustomPeriodParams);

        barQuoteHandler.observableForFilters(quoteFilters).subscribe(quoteSubscriber);
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
        verify(jforexUtilMock).subscribeToBarsFeed(quoteEURUSDCustomPeriodParams);
    }
}
