package com.jforex.programming.quote;

import java.util.Map;
import java.util.Set;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.HistoryUtil;

import rx.Observable;

public class TickQuoteRepository {

    private final Map<Instrument, TickQuote> tickQuotes;

    public TickQuoteRepository(final Observable<TickQuote> tickQuoteObservable,
                               final HistoryUtil historyUtil,
                               final Set<Instrument> subscribedInstruments) {
        tickQuotes = historyUtil.tickQuotes(subscribedInstruments);
        tickQuoteObservable.subscribe(this::onTickQuote);
    }

    private void onTickQuote(final TickQuote tickQuote) {
        tickQuotes.put(tickQuote.instrument(), tickQuote);
    }

    public ITick get(final Instrument instrument) {
        return tickQuotes.get(instrument).tick();
    }
}
