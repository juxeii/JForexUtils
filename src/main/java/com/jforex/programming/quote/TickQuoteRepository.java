package com.jforex.programming.quote;

import java.util.Map;
import java.util.Set;

import com.jforex.programming.misc.HistoryUtil;

import com.dukascopy.api.Instrument;

import rx.Observable;

public class TickQuoteRepository {

    private final Map<Instrument, TickQuote> quotesByInstrument;

    public TickQuoteRepository(final Observable<TickQuote> tickQuoteObservable,
                               final HistoryUtil historyUtil,
                               final Set<Instrument> subscribedInstruments) {
        quotesByInstrument = historyUtil.tickQuotes(subscribedInstruments);

        tickQuoteObservable.subscribe(this::onTickQuote);
    }

    private final void onTickQuote(final TickQuote aNewQuote) {
        quotesByInstrument.put(aNewQuote.instrument(), aNewQuote);
    }

    public TickQuote get(final Instrument instrument) {
        return quotesByInstrument.get(instrument);
    }
}
