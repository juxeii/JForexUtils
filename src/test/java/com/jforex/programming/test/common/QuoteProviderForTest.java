package com.jforex.programming.test.common;

import static info.solidsoft.mockito.java8.LambdaMatcher.argLambda;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.test.fakes.IBarForTest;
import com.jforex.programming.test.fakes.ITickForTest;

public class QuoteProviderForTest extends CurrencyUtilForTest {

    public final ITick tickEURUSD = new ITickForTest(bidEURUSD, askEURUSD);
    public final ITick tickAUDUSD = new ITickForTest(bidAUDUSD, askAUDUSD);

    public final TickQuote tickQuoteEURUSD = new TickQuote(instrumentEURUSD, tickEURUSD);
    public final TickQuote tickQuoteAUDUSD = new TickQuote(instrumentAUDUSD, tickAUDUSD);

    public final IBar askBarEURUSD = new IBarForTest();
    public final IBar bidBarEURUSD = new IBarForTest();
    public final IBar askBarAUDUSD = new IBarForTest();
    public final IBar bidBarAUDUSD = new IBarForTest();

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

    public static void setBarQuoteExpectations(final BarQuoteHandler barQuoteProviderMock,
                                               final Instrument instrument,
                                               final Period period,
                                               final IBar askBar,
                                               final IBar bidBar) {
        when(barQuoteProviderMock.quote(argLambda(td -> td.instrument() == instrument
                && td.period() == period
                && td.offerSide() == OfferSide.ASK))).thenReturn(askBar);
        when(barQuoteProviderMock.quote(argLambda(td -> td.instrument() == instrument
                && td.period() == period
                && td.offerSide() == OfferSide.BID))).thenReturn(bidBar);
    }

    public static IBar barPriceExpectations(final double open,
                                            final double high,
                                            final double low,
                                            final double close) {
        final IBar barMock = mock(IBar.class);
        when(barMock.getOpen()).thenReturn(open);
        when(barMock.getHigh()).thenReturn(high);
        when(barMock.getLow()).thenReturn(low);
        when(barMock.getClose()).thenReturn(close);

        return barMock;
    }
}
