package com.jforex.programming.quote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.keyvalue.MultiKey;

import com.jforex.programming.misc.HistoryUtil;

import com.dukascopy.api.IBar;

import rx.Observable;

public class BarQuoteRepository {

    private final HistoryUtil historyUtil;
    private final Map<MultiKey<Object>, BarQuote> barQuotes = new ConcurrentHashMap<>();

    public BarQuoteRepository(final Observable<BarQuote> barQuoteObservable,
                              final HistoryUtil historyUtil) {
        this.historyUtil = historyUtil;

        barQuoteObservable.subscribe(this::onBarQuote);
    }

    private void onBarQuote(final BarQuote barQuote) {
        final MultiKey<Object> quoteKey = new MultiKey<Object>(barQuote.instrument(),
                                                               barQuote.period(),
                                                               barQuote.offerSide());
        barQuotes.put(quoteKey, barQuote);
    }

    public BarQuote get(final BarQuoteParams barQuoteFilter) {
        final MultiKey<Object> quoteKey = new MultiKey<Object>(barQuoteFilter.instrument(),
                                                               barQuoteFilter.period(),
                                                               barQuoteFilter.offerSide());
        return barQuotes.containsKey(quoteKey)
                ? barQuotes.get(quoteKey)
                : quoteFromHistory(barQuoteFilter);
    }

    private BarQuote quoteFromHistory(final BarQuoteParams barQuoteParams) {
        final IBar historyBar = historyUtil.latestBar(barQuoteParams);
        final BarQuote barQuote = new BarQuote(barQuoteParams, historyBar);

        onBarQuote(barQuote);

        return barQuote;
    }
}
