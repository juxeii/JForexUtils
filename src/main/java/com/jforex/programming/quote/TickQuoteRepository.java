package com.jforex.programming.quote;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.HistoryUtil;

import io.reactivex.Observable;

public class TickQuoteRepository {

    private final HistoryUtil historyUtil;
    private final Map<Instrument, TickQuote> quotesByInstrument = new ConcurrentHashMap<>();

    private final static Logger logger = LogManager.getLogger(TickQuoteRepository.class);

    public TickQuoteRepository(final Observable<TickQuote> tickQuoteObservable,
                               final HistoryUtil historyUtil,
                               final Set<Instrument> subscribedInstruments) {
        this.historyUtil = historyUtil;

        historyUtil
            .tickQuotesObservable(subscribedInstruments)
            .subscribe(this::onTickQuote);
        tickQuoteObservable.subscribe(this::onTickQuote);
    }

    private final void onTickQuote(final TickQuote tickQuote) {
        quotesByInstrument.put(tickQuote.instrument(), tickQuote);
    }

    public TickQuote get(final Instrument instrument) {
        return quotesByInstrument.containsKey(instrument)
                ? quotesByInstrument.get(instrument)
                : quoteFromHistory(instrument);

    }

    private TickQuote quoteFromHistory(final Instrument instrument) {
        logger.debug("Trying to get tick quote for " + instrument + " from history...");

        TickQuote tickQuote = null;
        try {
            tickQuote = historyUtil
                .tickQuoteObservable(instrument)
                .blockingFirst();
            onTickQuote(tickQuote);
        } catch (final Exception e) {
            logger.error("Could not get historical quote for " + instrument + "!" + e.getMessage());
        }

        return tickQuote;
    }

    public Map<Instrument, TickQuote> getAll() {
        return Collections.unmodifiableMap(quotesByInstrument);
    }
}
