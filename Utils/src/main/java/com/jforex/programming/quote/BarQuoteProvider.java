package com.jforex.programming.quote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.keyvalue.MultiKey;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import rx.Observable;

public class BarQuoteProvider {

    private final Observable<BarQuote> barObservable;
    private final IHistory history;
    private final Map<MultiKey<Object>, BarQuote> latestBarWithPeriodByInstrument = new ConcurrentHashMap<>();

    public BarQuoteProvider(final Observable<BarQuote> barObservable,
                          final IHistory history) {
        this.barObservable = barObservable;
        this.history = history;

        barObservable.subscribe(this::onBarQuote);
    }

    public IBar askBar(final Instrument instrument,
                       final Period period) {
        return bar(instrument, period, OfferSide.ASK);
    }

    public IBar bidBar(final Instrument instrument,
                       final Period period) {
        return bar(instrument, period, OfferSide.BID);
    }

    public IBar forOfferSide(final Instrument instrument,
                             final Period period,
                             final OfferSide offerSide) {
        return offerSide == OfferSide.ASK ? askBar(instrument, period) : bidBar(instrument, period);
    }

    private IBar bar(final Instrument instrument,
                     final Period period,
                     final OfferSide offerSide) {
        return latestBarWithPeriodByInstrument.containsKey(barKey(instrument, period))
                ? barFromStore(instrument, period, offerSide)
                : barFromHistory(instrument, period, offerSide);
    }

    private IBar barFromHistory(final Instrument instrument,
                                final Period period,
                                final OfferSide offerSide) {
        try {
            final IBar historyBar = history.getBar(instrument, period, offerSide, 1);
            if (historyBar == null)
                throw new QuoteProviderException("Last bar for " + instrument + " and period " + period
                        + " from history returned null!");
            return historyBar;
        } catch (final JFException e) {
            throw new QuoteProviderException("Exception occured while retreiving bar for " + instrument
                    + " Message: " + e.getMessage());
        }
    }

    private IBar barFromStore(final Instrument instrument,
                              final Period period,
                              final OfferSide offerSide) {
        final BarQuote barQuote = latestBarWithPeriodByInstrument.get(barKey(instrument, period));
        return offerSide == OfferSide.ASK ? barQuote.askBar() : barQuote.bidBar();
    }

    private void onBarQuote(final BarQuote barQuote) {
        final MultiKey<Object> multiKey = barKey(barQuote.instrument(), barQuote.period());
        latestBarWithPeriodByInstrument.put(multiKey, barQuote);
    }

    public void subscribe(final Instrument instrument,
                          final Period period,
                          final BarQuoteConsumer barQuoteConsumer) {
        barObservable.filter(barQuote -> instrument == barQuote.instrument() && period == barQuote.period())
                     .subscribe(barQuoteConsumer::onBarQuote);
    }

    public Observable<BarQuote> observable() {
        return barObservable;
    }

    private MultiKey<Object> barKey(final Instrument instrument,
                                    final Period period) {
        return new MultiKey<Object>(instrument, period);
    }
}
