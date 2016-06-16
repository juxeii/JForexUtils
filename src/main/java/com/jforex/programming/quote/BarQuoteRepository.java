package com.jforex.programming.quote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.keyvalue.MultiKey;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
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

    public IBar get(final Instrument instrument,
                    final Period period,
                    final OfferSide offerSide) {
        return barQuotes.containsKey(new MultiKey<Object>(instrument, period, offerSide))
                ? barQuoteByOfferSide(instrument, period, offerSide)
                : barQuoteFromHistory(instrument, period, offerSide);
    }

    private IBar barQuoteByOfferSide(final Instrument instrument,
                                     final Period period,
                                     final OfferSide offerSide) {
        final BarQuote barQuote = barQuotes.get(new MultiKey<Object>(instrument, period, offerSide));
        return barQuote.bar();
    }

    private IBar barQuoteFromHistory(final Instrument instrument,
                                     final Period period,
                                     final OfferSide offerSide) {
        final IBar historyBar = historyUtil.latestBar(instrument, period, offerSide);
        onBarQuote(new BarQuote(instrument, period, offerSide, historyBar));
        return historyBar;
    }
}
