package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.jforex.programming.misc.HistoryUtil;
import com.jforex.programming.quote.BarQuoteParams;
import com.jforex.programming.quote.QuoteProviderException;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.test.common.QuoteProviderForTest;
import com.jforex.programming.test.fakes.IBarForTest;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class HistoryUtilTest extends QuoteProviderForTest {

    private HistoryUtil historyUtil;

    private final BarQuoteParams barQuoteParams =
            BarQuoteParams
                    .forInstrument(instrumentEURUSD)
                    .period(Period.THIRTY_MINS)
                    .offerSide(OfferSide.ASK);

    @Before
    public void setUp() {
        initCommonTestFramework();

        historyUtil = new HistoryUtil(historyMock);
    }

    @Test
    public void latestTickIsCorrect() throws JFException {
        when(historyMock.getLastTick(instrumentEURUSD)).thenReturn(tickEURUSD);

        final ITick latestTick = historyUtil.latestTick(instrumentEURUSD);

        assertThat(latestTick, equalTo(tickEURUSD));
    }

    @Test
    public void latestTickWithRetriesIsCorrect() throws JFException {
        when(historyMock.getLastTick(instrumentEURUSD))
                .thenThrow(jfException)
                .thenThrow(jfException)
                .thenThrow(jfException)
                .thenReturn(tickEURUSD);

        final ITick latestTick = historyUtil.latestTick(instrumentEURUSD);

        assertThat(latestTick, equalTo(tickEURUSD));
        verify(historyMock, times(4)).getLastTick(instrumentEURUSD);
    }

    @Test(expected = QuoteProviderException.class)
    public void latestTickThrowsWhenHistoryReturnsNullTick() throws JFException {
        when(historyMock.getLastTick(instrumentEURUSD)).thenReturn(null);

        historyUtil.latestTick(instrumentEURUSD);
    }

    @Test
    public void tickQuotesMapIsCorrect() throws JFException {
        final Set<Instrument> instruments = Sets.newHashSet(instrumentEURUSD, instrumentAUDUSD);
        when(historyMock.getLastTick(instrumentEURUSD)).thenReturn(tickEURUSD);
        when(historyMock.getLastTick(instrumentAUDUSD)).thenReturn(tickAUDUSD);

        final Map<Instrument, TickQuote> tickQuotes = historyUtil.tickQuotes(instruments);

        assertThat(tickQuotes.size(), equalTo(2));

        assertThat(tickQuotes.get(instrumentEURUSD).instrument(), equalTo(instrumentEURUSD));
        assertThat(tickQuotes.get(instrumentEURUSD).tick(), equalTo(tickEURUSD));

        assertThat(tickQuotes.get(instrumentAUDUSD).instrument(), equalTo(instrumentAUDUSD));
        assertThat(tickQuotes.get(instrumentAUDUSD).tick(), equalTo(tickAUDUSD));
    }

    @Test
    public void latestBarIsCorrect() throws JFException {
        final IBar testBar = new IBarForTest();
        when(historyMock.getBar(instrumentEURUSD, Period.THIRTY_MINS, OfferSide.ASK, 1))
                .thenReturn(testBar);

        final IBar latestBar = historyUtil.latestBar(barQuoteParams);

        assertThat(latestBar, equalTo(testBar));
    }

    @Test
    public void latestBarWithRetriesIsCorrect() throws JFException {
        final IBar testBar = new IBarForTest();
        when(historyMock.getBar(instrumentEURUSD, Period.THIRTY_MINS, OfferSide.ASK, 1))
                .thenThrow(jfException)
                .thenThrow(jfException)
                .thenThrow(jfException)
                .thenReturn(testBar);

        final IBar latestBar = historyUtil.latestBar(barQuoteParams);

        assertThat(latestBar, equalTo(testBar));
        verify(historyMock, times(4)).getBar(instrumentEURUSD,
                                             Period.THIRTY_MINS,
                                             OfferSide.ASK,
                                             1);
    }

    @Test(expected = QuoteProviderException.class)
    public void latestBarThrowsWhenHistoryReturnsNullBar() throws JFException {
        when(historyMock.getBar(instrumentEURUSD, Period.THIRTY_MINS, OfferSide.ASK, 1))
                .thenReturn(null);

        historyUtil.latestBar(barQuoteParams);
    }
}
