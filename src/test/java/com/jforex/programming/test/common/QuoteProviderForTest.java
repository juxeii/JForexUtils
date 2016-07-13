package com.jforex.programming.test.common;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.test.fakes.ITickForTest;

public class QuoteProviderForTest extends CurrencyUtilForTest {

    public final ITick tickEURUSD = new ITickForTest(bidEURUSD, askEURUSD);
    public final ITick tickAUDUSD = new ITickForTest(bidAUDUSD, askAUDUSD);

    public final TickQuote tickQuoteEURUSD = new TickQuote(instrumentEURUSD, tickEURUSD);
    public final TickQuote tickQuoteAUDUSD = new TickQuote(instrumentAUDUSD, tickAUDUSD);

    public final IBar askBarEURUSD = mock(IBar.class);
    public final IBar bidBarEURUSD = mock(IBar.class);
    public final IBar askBarAUDUSD = mock(IBar.class);
    public final IBar bidBarAUDUSD = mock(IBar.class);

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
