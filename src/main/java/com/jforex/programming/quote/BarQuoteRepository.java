package com.jforex.programming.quote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.keyvalue.MultiKey;

import com.dukascopy.api.IBar;
import com.jforex.programming.builder.BarQuoteFilter;
import com.jforex.programming.misc.HistoryUtil;

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
        final MultiKey<Object> multiKey = new MultiKey<Object>(barQuote.instrument(),
                                                               barQuote.period(),
                                                               barQuote.offerSide());
        barQuotes.put(multiKey, barQuote);
    }

    public IBar get(final BarQuoteFilter barQuoteFilter) {
        final MultiKey<Object> quoteKey = quoteKeyFromFilter(barQuoteFilter);
        return barQuotes.containsKey(quoteKey)
                ? barQuotes.get(quoteKey).bar()
                : barFromHistory(barQuoteFilter);
    }

    private MultiKey<Object> quoteKeyFromFilter(final BarQuoteFilter barQuoteFilter) {
        return new MultiKey<Object>(barQuoteFilter.instrument(),
                                    barQuoteFilter.period(),
                                    barQuoteFilter.offerSide());
    }

    private IBar barFromHistory(final BarQuoteFilter barQuoteFilter) {
        final IBar historyBar = historyUtil.latestBar(barQuoteFilter);
        onBarQuote(new BarQuote(barQuoteFilter.instrument(),
                                barQuoteFilter.period(),
                                barQuoteFilter.offerSide(),
                                historyBar));
        return historyBar;
    }
}
