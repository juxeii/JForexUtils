package com.jforex.programming.test.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteParams;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.test.fakes.ITickForTest;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.Unit;

public class QuoteProviderForTest extends CurrencyUtilForTest {

    public final ITick tickEURUSD = new ITickForTest(bidEURUSD, askEURUSD);
    public final ITick tickAUDUSD = new ITickForTest(bidAUDUSD, askAUDUSD);

    public final TickQuote tickQuoteEURUSD = new TickQuote(instrumentEURUSD, tickEURUSD);
    public final TickQuote tickQuoteAUDUSD = new TickQuote(instrumentAUDUSD, tickAUDUSD);

    public final IBar askBarEURUSD = mock(IBar.class);
    public final IBar bidBarEURUSD = mock(IBar.class);
    public final IBar askBarAUDUSD = mock(IBar.class);
    public final IBar bidBarAUDUSD = mock(IBar.class);

    public final Period barQuotePeriod = Period.FIVE_MINS;
    public final Period custom3MinutePeriod = Period.createCustomPeriod(Unit.Minute, 3);
    public final BarQuoteParams askBarEURUSDParams = BarQuoteParams
            .forInstrument(instrumentEURUSD)
            .period(barQuotePeriod)
            .offerSide(OfferSide.ASK);
    public final BarQuoteParams bidBarEURUSDParams = BarQuoteParams
            .forInstrument(instrumentEURUSD)
            .period(barQuotePeriod)
            .offerSide(OfferSide.BID);
    public final BarQuoteParams askBarAUDUSDParams = BarQuoteParams
            .forInstrument(instrumentAUDUSD)
            .period(barQuotePeriod)
            .offerSide(OfferSide.ASK);
    public final BarQuoteParams bidBarAUDUSDParams = BarQuoteParams
            .forInstrument(instrumentAUDUSD)
            .period(barQuotePeriod)
            .offerSide(OfferSide.BID);
    public final BarQuoteParams askBarEURUSDCustomPeriodParams = BarQuoteParams
            .forInstrument(instrumentEURUSD)
            .period(custom3MinutePeriod)
            .offerSide(OfferSide.ASK);

    public final BarQuote askBarQuoteEURUSD = new BarQuote(askBarEURUSDParams, askBarEURUSD);
    public final BarQuote bidBarQuoteEURUSD = new BarQuote(bidBarEURUSDParams, bidBarEURUSD);
    public final BarQuote askBarQuoteAUDUSD = new BarQuote(askBarAUDUSDParams, askBarAUDUSD);
    public final BarQuote bidBarQuoteAUDUSD = new BarQuote(bidBarAUDUSDParams, bidBarAUDUSD);
    public final BarQuote askBarQuoteEURUSDCustomPeriod =
            new BarQuote(askBarEURUSDCustomPeriodParams, askBarEURUSD);

    public void assertEqualBarQuotes(final BarQuote receivedBarQuote,
                                     final BarQuote expectedBarQuote) {
        assertThat(receivedBarQuote.instrument(),
                   equalTo(expectedBarQuote.instrument()));
        assertThat(receivedBarQuote.period(),
                   equalTo(expectedBarQuote.period()));
        assertThat(receivedBarQuote.offerSide(),
                   equalTo(expectedBarQuote.offerSide()));
        assertThat(receivedBarQuote.bar(),
                   equalTo(expectedBarQuote.bar()));
    }

    public void assertEqualTickQuotes(final TickQuote receivedTickQuote,
                                      final TickQuote expectedTickQuote) {
        assertThat(receivedTickQuote.instrument(),
                   equalTo(expectedTickQuote.instrument()));
        assertThat(receivedTickQuote.tick(),
                   equalTo(expectedTickQuote.tick()));
    }

    public static void setQuoteExpectations(final TickQuoteHandler quoteProviderMock,
                                            final Instrument instrument,
                                            final double bid,
                                            final double ask) {
        final ITick tick = new ITickForTest(bid, ask);
        when(quoteProviderMock.tick(instrument)).thenReturn(tick);
        when(quoteProviderMock.ask(instrument)).thenReturn(ask);
        when(quoteProviderMock.bid(instrument)).thenReturn(bid);
        when(quoteProviderMock.forOfferSide(instrument, OfferSide.ASK)).thenReturn(ask);
        when(quoteProviderMock.forOfferSide(instrument, OfferSide.BID)).thenReturn(bid);
    }
}
