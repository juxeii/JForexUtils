package com.jforex.programming.test.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.test.fakes.ITickForTest;

public class QuoteProviderForTest {

    public static void setQuoteExpectations(final TickQuoteProvider quoteProviderMock,
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

    public static void setBarQuoteExpectations(final BarQuoteProvider barQuoteProviderMock,
                                               final Instrument instrument,
                                               final Period period,
                                               final IBar askBar,
                                               final IBar bidBar) {
        when(barQuoteProviderMock.askBar(instrument, period)).thenReturn(askBar);
        when(barQuoteProviderMock.bidBar(instrument, period)).thenReturn(bidBar);
        when(barQuoteProviderMock.forOfferSide(instrument, period, OfferSide.ASK)).thenReturn(askBar);
        when(barQuoteProviderMock.forOfferSide(instrument, period, OfferSide.BID)).thenReturn(bidBar);
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
