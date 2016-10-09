package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.HistoryUtil;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.test.common.QuoteProviderForTest;
import com.jforex.programming.test.common.RxTestUtil;

import io.reactivex.observers.TestObserver;

public class HistoryUtilTest extends QuoteProviderForTest {

    private HistoryUtil historyUtil;

    private final TestObserver<ITick> tickSubscriber = TestObserver.create();
    private final TestObserver<IBar> barSubscriber = TestObserver.create();

    @Before
    public void setUp() {
        historyUtil = new HistoryUtil(historyMock);
    }

    private void assertTickSubscriber() {
        tickSubscriber.assertNoErrors();
        tickSubscriber.assertComplete();

        tickSubscriber.assertValue(tickEURUSD);
    }

    private void assertBarSubscriber() {
        barSubscriber.assertNoErrors();
        barSubscriber.assertComplete();

        barSubscriber.assertValue(askBarEURUSD);
    }

    @Test
    public void latestTickIsCorrect() throws JFException {
        when(historyMock.getLastTick(instrumentEURUSD))
            .thenReturn(tickEURUSD);

        historyUtil
            .lastestTickObservable(instrumentEURUSD)
            .subscribe(tickSubscriber);

        assertTickSubscriber();
    }

    @Test
    public void latestTickWithRetriesIsCorrect() throws JFException {
        when(historyMock.getLastTick(instrumentEURUSD))
            .thenThrow(jfException)
            .thenThrow(jfException)
            .thenReturn(null)
            .thenReturn(tickEURUSD);

        historyUtil
            .lastestTickObservable(instrumentEURUSD)
            .subscribe(tickSubscriber);

        RxTestUtil.advanceTimeInMillisBy(5000L);

        verify(historyMock, times(4)).getLastTick(instrumentEURUSD);
        assertTickSubscriber();
    }

    @Test
    public void tickQuotesMapIsCorrect() throws JFException {
        final Set<Instrument> instruments = Sets.newHashSet(instrumentEURUSD, instrumentAUDUSD);
        when(historyMock.getLastTick(instrumentEURUSD))
            .thenReturn(tickEURUSD);
        when(historyMock.getLastTick(instrumentAUDUSD))
            .thenReturn(tickAUDUSD);
        final Map<Instrument, TickQuote> quotesByInstrument = new HashMap<>();

        historyUtil
            .tickQuotesObservable(instruments)
            .subscribe(quote -> quotesByInstrument.put(quote.instrument(), quote));

        assertThat(quotesByInstrument.size(), equalTo(2));
        assertThat(quotesByInstrument.get(instrumentEURUSD),
                   equalTo(tickQuoteEURUSD));
        assertThat(quotesByInstrument.get(instrumentAUDUSD),
                   equalTo(tickQuoteAUDUSD));
    }

    @Test
    public void latestBarIsCorrect() throws JFException {
        when(historyMock.getBar(instrumentEURUSD, barQuotePeriod, OfferSide.ASK, 1))
            .thenReturn(askBarEURUSD);

        historyUtil
            .latestBarObservable(askBarEURUSDParams)
            .subscribe(barSubscriber);

        assertBarSubscriber();
    }

    @Test
    public void latestBarWithRetriesIsCorrect() throws JFException {
        when(historyMock.getBar(instrumentEURUSD, barQuotePeriod, OfferSide.ASK, 1))
            .thenThrow(jfException)
            .thenThrow(jfException)
            .thenReturn(null)
            .thenReturn(askBarEURUSD);

        historyUtil
            .latestBarObservable(askBarEURUSDParams)
            .subscribe(barSubscriber);

        RxTestUtil.advanceTimeInMillisBy(5000L);

        verify(historyMock, times(4)).getBar(instrumentEURUSD,
                                             barQuotePeriod,
                                             OfferSide.ASK,
                                             1);

        assertBarSubscriber();
    }
}
